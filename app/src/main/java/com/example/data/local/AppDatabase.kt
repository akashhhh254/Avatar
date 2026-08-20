package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        ConversationMemberEntity::class,
        MessageEntity::class,
        MessageReactionEntity::class,
        CallLogEntity::class,
        EncryptionKeyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "avatar_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val min10Ago = now - (10 * 60 * 1000)
            val hour1Ago = now - (60 * 60 * 1000)
            val hour3Ago = now - (3 * 60 * 60 * 1000)
            val day1Ago = now - (24 * 60 * 60 * 1000)

            // Current user default profile
            val currentUser = UserEntity(
                id = "user_me",
                phoneNumber = "+1 555-0199",
                username = "alex_v",
                displayName = "Alex Vance",
                avatarUrl = "",
                bio = "🔒 Encrypted with AVATAR Shield",
                isOnline = true,
                isCurrentUser = true
            )

            // Initial Contacts
            val contact1 = UserEntity(
                id = "user_1",
                phoneNumber = "+1 555-0142",
                username = "elena_r",
                displayName = "Elena Rostova",
                avatarUrl = "",
                bio = "Architecting the future | AVATAR E2E",
                isOnline = true,
                lastSeen = now
            )
            val contact2 = UserEntity(
                id = "user_2",
                phoneNumber = "+1 555-0188",
                username = "marcus_k",
                displayName = "Marcus Chen",
                avatarUrl = "",
                bio = "Cryptography & Distributed Systems",
                isOnline = false,
                lastSeen = min10Ago
            )
            val contact3 = UserEntity(
                id = "user_3",
                phoneNumber = "+1 555-0176",
                username = "sarah_p",
                displayName = "Sarah Palmer",
                avatarUrl = "",
                bio = "UI/UX Designer @ AVATAR Studio",
                isOnline = true,
                lastSeen = now
            )
            val contact4 = UserEntity(
                id = "user_4",
                phoneNumber = "+1 555-0123",
                username = "david_b",
                displayName = "David Vance",
                avatarUrl = "",
                bio = "Building resilient networks",
                isOnline = false,
                lastSeen = day1Ago
            )

            db.userDao().insertUsers(listOf(currentUser, contact1, contact2, contact3, contact4))

            // Conversations
            val conv1 = ConversationEntity(
                id = "conv_1",
                title = "Elena Rostova",
                isGroup = false,
                lastMessageText = "The double ratchet key exchange verified cleanly. 🔒",
                lastMessageTimestamp = now - (2 * 60 * 1000),
                unreadCount = 1,
                isPinned = true,
                otherUserId = "user_1"
            )

            val conv2 = ConversationEntity(
                id = "conv_2",
                title = "Marcus Chen",
                isGroup = false,
                lastMessageText = "Are we pushing the Zero-Trust release build today?",
                lastMessageTimestamp = min10Ago,
                unreadCount = 0,
                isPinned = false,
                otherUserId = "user_2"
            )

            val conv3 = ConversationEntity(
                id = "conv_group_1",
                title = "AVATAR Core Engineering ⚡",
                isGroup = true,
                lastMessageText = "Sarah: Updated the dark slate glassmorphism theme components!",
                lastMessageTimestamp = hour1Ago,
                unreadCount = 3,
                isPinned = true,
                description = "Official E2E Protocol Core Team Group"
            )

            db.conversationDao().insertConversations(listOf(conv1, conv2, conv3))

            // Group members
            val groupMembers = listOf(
                ConversationMemberEntity("conv_group_1", "user_me", role = "ADMIN"),
                ConversationMemberEntity("conv_group_1", "user_1", role = "ADMIN"),
                ConversationMemberEntity("conv_group_1", "user_2", role = "MEMBER"),
                ConversationMemberEntity("conv_group_1", "user_3", role = "MEMBER")
            )
            groupMembers.forEach {
                db.openHelper.writableDatabase.execSQL(
                    "INSERT OR REPLACE INTO conversation_members (conversationId, userId, role, joinedAt) VALUES ('${it.conversationId}', '${it.userId}', '${it.role}', ${it.joinedAt})"
                )
            }

            // Initial Messages in Elena's chat
            val msg1 = MessageEntity(
                id = "msg_1",
                conversationId = "conv_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                content = "Hey Alex! Did you review the E2E encryption architecture for AVATAR?",
                timestamp = hour3Ago,
                status = "READ",
                isEncrypted = true
            )
            val msg2 = MessageEntity(
                id = "msg_2",
                conversationId = "conv_1",
                senderId = "user_me",
                senderName = "Alex Vance",
                content = "Yes, AES-256-GCM with ECDH key negotiation works flawlessly locally! Zero server exposure.",
                timestamp = hour3Ago + (2 * 60 * 1000),
                status = "READ",
                isEncrypted = true
            )
            val msg3 = MessageEntity(
                id = "msg_3",
                conversationId = "conv_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                content = "The double ratchet key exchange verified cleanly. 🔒",
                timestamp = now - (2 * 60 * 1000),
                status = "DELIVERED",
                isEncrypted = true
            )

            // Group messages
            val msgG1 = MessageEntity(
                id = "msg_g1",
                conversationId = "conv_group_1",
                senderId = "user_2",
                senderName = "Marcus Chen",
                content = "All unit tests for room persistence and background sync passed.",
                timestamp = hour1Ago - (15 * 60 * 1000),
                status = "READ",
                isEncrypted = true
            )
            val msgG2 = MessageEntity(
                id = "msg_g2",
                conversationId = "conv_group_1",
                senderId = "user_3",
                senderName = "Sarah Palmer",
                content = "Updated the dark slate glassmorphism theme components!",
                timestamp = hour1Ago,
                status = "DELIVERED",
                isEncrypted = true
            )

            db.messageDao().insertMessages(listOf(msg1, msg2, msg3, msgG1, msgG2))

            // Initial Call Logs
            val call1 = CallLogEntity(
                id = "call_1",
                participantId = "user_1",
                participantName = "Elena Rostova",
                callType = "VIDEO",
                isOutgoing = false,
                isMissed = false,
                timestamp = hour3Ago,
                durationSeconds = 245
            )
            val call2 = CallLogEntity(
                id = "call_2",
                participantId = "user_2",
                participantName = "Marcus Chen",
                callType = "AUDIO",
                isOutgoing = true,
                isMissed = false,
                timestamp = day1Ago,
                durationSeconds = 120
            )
            val call3 = CallLogEntity(
                id = "call_3",
                participantId = "user_3",
                participantName = "Sarah Palmer",
                callType = "VIDEO",
                isOutgoing = false,
                isMissed = true,
                timestamp = day1Ago - (2 * 60 * 60 * 1000),
                durationSeconds = 0
            )

            db.callDao().insertCallLog(call1)
            db.callDao().insertCallLog(call2)
            db.callDao().insertCallLog(call3)
        }
    }
}

package com.example.repository

import android.content.Context
import com.example.crypto.EncryptionEngine
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.network.RealtimeChatEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val callDao = db.callDao()

    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getAllActiveConversations()
    val groupConversations: Flow<List<ConversationEntity>> = conversationDao.getGroupConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
    val allContacts: Flow<List<UserEntity>> = userDao.getAllContacts()
    val blockedContacts: Flow<List<UserEntity>> = userDao.getBlockedUsers()
    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    val callLogs: Flow<List<CallLogEntity>> = callDao.getAllCallLogs()

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserFlow(userId)

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId).map { messages ->
            messages.map { msg ->
                if (msg.isEncrypted && !msg.isDeleted) {
                    val decryptedContent = EncryptionEngine.decryptPayload(msg.content)
                    msg.copy(content = decryptedContent)
                } else {
                    msg
                }
            }
        }
    }

    fun getConversationFlow(conversationId: String): Flow<ConversationEntity?> {
        return conversationDao.getConversationFlow(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        messageType: String = "TEXT",
        mediaUrl: String = "",
        mediaName: String = "",
        mediaSize: String = "",
        mediaDurationSeconds: Int = 0,
        replyToId: String = "",
        replyToPreview: String = ""
    ) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val encryptedPayload = EncryptionEngine.encryptPayload(content)

        val message = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = currentUser.id,
            senderName = currentUser.displayName,
            senderAvatar = currentUser.avatarUrl,
            content = encryptedPayload,
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            isEncrypted = true,
            messageType = messageType,
            mediaUrl = mediaUrl,
            mediaName = mediaName,
            mediaSize = mediaSize,
            mediaDurationSeconds = mediaDurationSeconds,
            replyToId = replyToId,
            replyToPreview = replyToPreview
        )

        messageDao.insertMessage(message)

        // Update conversation last message preview
        val displayPreview = when (messageType) {
            "IMAGE" -> "📷 Photo"
            "VIDEO" -> "🎥 Video"
            "AUDIO", "VOICE" -> "🎙️ Voice message (${mediaDurationSeconds}s)"
            "DOCUMENT" -> "📄 $mediaName"
            "LOCATION" -> "📍 Location shared"
            else -> content
        }

        val conv = conversationDao.getConversation(conversationId)
        if (conv != null) {
            conversationDao.updateConversation(
                conv.copy(
                    lastMessageText = displayPreview,
                    lastMessageTimestamp = message.timestamp
                )
            )

            // Trigger real-time reply simulation if 1-on-1 chat
            if (!conv.isGroup && conv.otherUserId.isNotEmpty()) {
                val otherUser = userDao.getUserById(conv.otherUserId)
                val peerName = otherUser?.displayName ?: conv.title

                RealtimeChatEngine.triggerSimulatedTypingAndReply(
                    conversationId = conversationId,
                    senderName = peerName
                ) { replyText ->
                    val replyMsgId = "msg_${UUID.randomUUID().toString().take(8)}"
                    val replyEncrypted = EncryptionEngine.encryptPayload(replyText)
                    val replyMsg = MessageEntity(
                        id = replyMsgId,
                        conversationId = conversationId,
                        senderId = conv.otherUserId,
                        senderName = peerName,
                        content = replyEncrypted,
                        timestamp = System.currentTimeMillis(),
                        status = "DELIVERED",
                        isEncrypted = true
                    )
                    suspend {
                        messageDao.insertMessage(replyMsg)
                        conversationDao.updateConversation(
                            conv.copy(
                                lastMessageText = replyText,
                                lastMessageTimestamp = replyMsg.timestamp
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun insertDirectMessage(msg: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(msg)
    }

    suspend fun togglePinConversation(conversationId: String, currentPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(conversationId, !currentPinned)
    }

    suspend fun toggleArchiveConversation(conversationId: String, currentArchived: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setArchived(conversationId, !currentArchived)
    }

    suspend fun toggleMuteConversation(conversationId: String, currentMuted: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setMuted(conversationId, !currentMuted)
    }

    suspend fun clearChatMessages(conversationId: String) = withContext(Dispatchers.IO) {
        messageDao.clearChatMessages(conversationId)
        val conv = conversationDao.getConversation(conversationId)
        if (conv != null) {
            conversationDao.updateConversation(conv.copy(lastMessageText = "Chat cleared"))
        }
    }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        messageDao.clearChatMessages(conversationId)
        conversationDao.deleteConversation(conversationId)
    }

    suspend fun editMessage(messageId: String, newContent: String) = withContext(Dispatchers.IO) {
        val encryptedNew = EncryptionEngine.encryptPayload(newContent)
        messageDao.editMessage(messageId, encryptedNew)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun addReaction(messageId: String, emoji: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        messageDao.insertReaction(
            MessageReactionEntity(
                messageId = messageId,
                userId = currentUser.id,
                emoji = emoji
            )
        )
    }

    suspend fun createNew1On1Chat(contact: UserEntity): String = withContext(Dispatchers.IO) {
        val convId = "conv_${contact.id}"
        val existing = conversationDao.getConversation(convId)
        if (existing != null) {
            return@withContext convId
        }

        val newConv = ConversationEntity(
            id = convId,
            title = contact.displayName,
            isGroup = false,
            avatarUrl = contact.avatarUrl,
            lastMessageText = "Conversation started with E2E Encryption 🔒",
            lastMessageTimestamp = System.currentTimeMillis(),
            otherUserId = contact.id
        )

        conversationDao.insertConversation(newConv)
        convId
    }

    suspend fun createGroupChat(title: String, description: String, selectedUserIds: List<String>): String = withContext(Dispatchers.IO) {
        val groupId = "conv_group_${UUID.randomUUID().toString().take(6)}"
        val currentUser = userDao.getCurrentUser()

        val groupConv = ConversationEntity(
            id = groupId,
            title = title,
            isGroup = true,
            description = description,
            lastMessageText = "Group created with AVATAR E2E Shield",
            lastMessageTimestamp = System.currentTimeMillis(),
            createdBy = currentUser?.id ?: "user_me"
        )

        conversationDao.insertConversation(groupConv)

        // Members
        val allMembers = selectedUserIds.toMutableList().apply {
            if (currentUser != null && !contains(currentUser.id)) add(currentUser.id)
        }

        allMembers.forEach { uid ->
            val role = if (uid == currentUser?.id) "ADMIN" else "MEMBER"
            db.openHelper.writableDatabase.execSQL(
                "INSERT OR REPLACE INTO conversation_members (conversationId, userId, role, joinedAt) VALUES ('$groupId', '$uid', '$role', ${System.currentTimeMillis()})"
            )
        }

        groupId
    }

    suspend fun blockUser(userId: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        userDao.setBlockedStatus(userId, isBlocked)
    }

    suspend fun addCallLog(participantId: String, participantName: String, callType: String, isOutgoing: Boolean) = withContext(Dispatchers.IO) {
        val call = CallLogEntity(
            id = "call_${UUID.randomUUID().toString().take(6)}",
            participantId = participantId,
            participantName = participantName,
            callType = callType,
            isOutgoing = isOutgoing,
            isMissed = false,
            timestamp = System.currentTimeMillis(),
            durationSeconds = (15..300).random()
        )
        callDao.insertCallLog(call)
    }

    suspend fun updateUserProfile(displayName: String, username: String, bio: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val updated = currentUser.copy(
            displayName = displayName,
            username = username,
            bio = bio
        )
        userDao.updateUser(updated)
    }

    fun searchGlobal(query: String): Flow<Map<String, Any>> {
        return conversationDao.searchConversations(query).map { convs ->
            mapOf("conversations" to convs)
        }
    }
}

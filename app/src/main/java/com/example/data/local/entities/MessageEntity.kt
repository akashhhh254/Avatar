package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // SENDING, SENT, DELIVERED, READ
    val isEncrypted: Boolean = true,
    val messageType: String = "TEXT", // TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, VOICE, LOCATION
    val mediaUrl: String = "",
    val mediaName: String = "",
    val mediaSize: String = "",
    val mediaDurationSeconds: Int = 0,
    val replyToId: String = "",
    val replyToPreview: String = "",
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isStarred: Boolean = false
)

package com.example.data.local.entities

import androidx.room.Entity

@Entity(tableName = "message_reactions", primaryKeys = ["messageId", "userId", "emoji"])
data class MessageReactionEntity(
    val messageId: String,
    val userId: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data.local.entities

import androidx.room.Entity

@Entity(tableName = "conversation_members", primaryKeys = ["conversationId", "userId"])
data class ConversationMemberEntity(
    val conversationId: String,
    val userId: String,
    val role: String = "MEMBER", // ADMIN, MEMBER
    val joinedAt: Long = System.currentTimeMillis()
)

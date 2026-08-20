package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val participantId: String,
    val participantName: String,
    val participantAvatar: String = "",
    val callType: String = "AUDIO", // AUDIO, VIDEO
    val isOutgoing: Boolean = false,
    val isMissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

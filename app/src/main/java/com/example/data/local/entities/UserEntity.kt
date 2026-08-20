package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val bio: String = "Hey there! I am using AVATAR.",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = false,
    val isBlocked: Boolean = false,
    val e2ePublicKey: String = ""
)

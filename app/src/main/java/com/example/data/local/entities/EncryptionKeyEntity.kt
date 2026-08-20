package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encryption_keys")
data class EncryptionKeyEntity(
    @PrimaryKey val keyId: String,
    val userId: String,
    val publicKeyPem: String,
    val privateKeyPemEncrypted: String,
    val createdAt: Long = System.currentTimeMillis()
)

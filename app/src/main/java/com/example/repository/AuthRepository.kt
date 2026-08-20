package com.example.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "avatar_auth_prefs")

class AuthRepository(private val context: Context) {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_PHONE = stringPreferencesKey("user_phone")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_ID = stringPreferencesKey("user_id")
    private val TWO_FACTOR_ENABLED = booleanPreferencesKey("two_factor_enabled")
    private val TWO_FACTOR_PIN = stringPreferencesKey("two_factor_pin")

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: true
    }

    val isTwoFactorEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TWO_FACTOR_ENABLED] ?: false
    }

    val savedPin: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TWO_FACTOR_PIN]
    }

    suspend fun verifyOtpAndLogin(identifier: String, isEmail: Boolean, otp: String): Boolean {
        if (otp.length == 6) {
            val db = AppDatabase.getInstance(context)
            val currentUser = db.userDao().getCurrentUser() ?: UserEntity(
                id = "user_me",
                phoneNumber = if (!isEmail) identifier else "+1 555-0199",
                username = "avatar_user",
                displayName = "AVATAR User",
                isCurrentUser = true
            )

            val updatedUser = if (isEmail) {
                currentUser.copy(isCurrentUser = true)
            } else {
                currentUser.copy(phoneNumber = identifier, isCurrentUser = true)
            }
            db.userDao().insertUser(updatedUser)

            context.dataStore.edit { prefs ->
                prefs[IS_LOGGED_IN] = true
                if (isEmail) {
                    prefs[USER_EMAIL] = identifier
                } else {
                    prefs[USER_PHONE] = identifier
                }
                prefs[USER_ID] = currentUser.id
            }
            return true
        }
        return false
    }

    suspend fun setTwoFactorAuth(enabled: Boolean, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[TWO_FACTOR_ENABLED] = enabled
            prefs[TWO_FACTOR_PIN] = pin
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
        }
    }
}


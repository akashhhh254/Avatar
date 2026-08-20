package com.example.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    CONNECTING,
    CONNECTED_ENCRYPTED,
    OFFLINE
}

data class TypingStatus(
    val conversationId: String,
    val userName: String,
    val isTyping: Boolean
)

object RealtimeChatEngine {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED_ENCRYPTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _typingStatus = MutableStateFlow<TypingStatus?>(null)
    val typingStatus: StateFlow<TypingStatus?> = _typingStatus.asStateFlow()

    fun toggleOfflineMode(goOffline: Boolean) {
        if (goOffline) {
            _connectionState.value = ConnectionState.OFFLINE
        } else {
            scope.launch {
                _connectionState.value = ConnectionState.CONNECTING
                delay(800)
                _connectionState.value = ConnectionState.CONNECTED_ENCRYPTED
            }
        }
    }

    fun triggerSimulatedTypingAndReply(
        conversationId: String,
        senderName: String,
        onReplyGenerated: (String) -> Unit
    ) {
        if (_connectionState.value == ConnectionState.OFFLINE) return

        scope.launch {
            delay(1200)
            _typingStatus.value = TypingStatus(conversationId, senderName, true)
            delay(2000)
            _typingStatus.value = TypingStatus(conversationId, senderName, false)

            val reply = generateAutoReply(senderName)
            onReplyGenerated(reply)
        }
    }

    private fun generateAutoReply(senderName: String): String {
        val replies = listOf(
            "Got it! Message received over AVATAR E2E tunnel 🔒",
            "Thanks for the update! All systems operational on my side.",
            "Sounds great! Let's touch base on this in our next meeting.",
            "Verified! Encrypted payload payload check passed ⚡",
            "Awesome, thanks $senderName!"
        )
        return replies.random()
    }
}

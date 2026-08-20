package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.network.ConnectionState
import com.example.network.RealtimeChatEngine
import com.example.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS,
    GROUPS,
    CALLS,
    CONTACTS
}

data class ActiveCallState(
    val isCallActive: Boolean = false,
    val participantName: String = "",
    val participantAvatar: String = "",
    val callType: String = "AUDIO", // AUDIO, VIDEO
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoCameraOn: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    val activeConversations: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupConversations: StateFlow<List<ConversationEntity>> = repository.groupConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = repository.archivedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<UserEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedContacts: StateFlow<List<UserEntity>> = repository.blockedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val callLogs: StateFlow<List<CallLogEntity>> = repository.callLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectionState: StateFlow<ConnectionState> = RealtimeChatEngine.connectionState
    val typingStatus = RealtimeChatEngine.typingStatus

    // Navigation & View State
    private val _selectedTab = MutableStateFlow(MainTab.CHATS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _activeConversation = MutableStateFlow<ConversationEntity?>(null)
    val activeConversation: StateFlow<ConversationEntity?> = _activeConversation.asStateFlow()

    private val _activeRecipientUser = MutableStateFlow<UserEntity?>(null)
    val activeRecipientUser: StateFlow<UserEntity?> = _activeRecipientUser.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    // Composer & Reply
    val composerText = MutableStateFlow("")
    val replyToMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage = MutableStateFlow<MessageEntity?>(null)

    // Voice recording
    val isVoiceRecording = MutableStateFlow(false)
    val voiceRecordingSeconds = MutableStateFlow(0)
    private var recordingJob: Job? = null

    // Calling state
    private val _activeCallState = MutableStateFlow(ActiveCallState())
    val activeCallState: StateFlow<ActiveCallState> = _activeCallState.asStateFlow()
    private var callTimerJob: Job? = null

    // Settings State
    val isDarkTheme = MutableStateFlow(true)
    val isAppLockEnabled = MutableStateFlow(false)
    val readReceiptsEnabled = MutableStateFlow(true)
    val lastSeenVisibility = MutableStateFlow("Everyone") // Everyone, Contacts, Nobody

    private var messagesJob: Job? = null
    private var recipientJob: Job? = null

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectConversation(conversationId: String?) {
        _selectedConversationId.value = conversationId
        if (conversationId == null) {
            _activeConversation.value = null
            _activeRecipientUser.value = null
            _messages.value = emptyList()
            messagesJob?.cancel()
            recipientJob?.cancel()
            return
        }

        recipientJob?.cancel()
        recipientJob = viewModelScope.launch {
            repository.getConversationFlow(conversationId).collect { conv ->
                _activeConversation.value = conv
                if (conv != null && !conv.isGroup && conv.otherUserId.isNotEmpty()) {
                    repository.getUserFlow(conv.otherUserId).collect { user ->
                        _activeRecipientUser.value = user
                    }
                } else {
                    _activeRecipientUser.value = null
                }
            }
        }

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForConversation(conversationId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun sendMessage(type: String = "TEXT", mediaUrl: String = "", mediaName: String = "", mediaSize: String = "", mediaDuration: Int = 0) {
        val convId = _selectedConversationId.value ?: return
        val text = composerText.value.trim()
        val reply = replyToMessage.value

        if (text.isEmpty() && type == "TEXT") return

        val editing = editingMessage.value
        if (editing != null) {
            viewModelScope.launch {
                repository.editMessage(editing.id, text)
                editingMessage.value = null
                composerText.value = ""
            }
            return
        }

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                content = if (text.isNotEmpty()) text else mediaName,
                messageType = type,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                mediaSize = mediaSize,
                mediaDurationSeconds = mediaDuration,
                replyToId = reply?.id ?: "",
                replyToPreview = reply?.content ?: ""
            )

            composerText.value = ""
            replyToMessage.value = null
        }
    }

    fun startVoiceRecording() {
        isVoiceRecording.value = true
        voiceRecordingSeconds.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (isVoiceRecording.value) {
                delay(1000)
                voiceRecordingSeconds.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        isVoiceRecording.value = false
        recordingJob?.cancel()
        voiceRecordingSeconds.value = 0
    }

    fun sendVoiceMessage() {
        val duration = voiceRecordingSeconds.value
        isVoiceRecording.value = false
        recordingJob?.cancel()

        if (duration >= 1) {
            sendMessage(
                type = "VOICE",
                mediaName = "Voice Note (${duration}s)",
                mediaSize = "${duration * 12} KB",
                mediaDuration = duration
            )
        }
        voiceRecordingSeconds.value = 0
    }

    fun togglePinConversation(conversationId: String, currentPinned: Boolean) {
        viewModelScope.launch { repository.togglePinConversation(conversationId, currentPinned) }
    }

    fun toggleArchiveConversation(conversationId: String, currentArchived: Boolean) {
        viewModelScope.launch { repository.toggleArchiveConversation(conversationId, currentArchived) }
    }

    fun toggleMuteConversation(conversationId: String, currentMuted: Boolean) {
        viewModelScope.launch { repository.toggleMuteConversation(conversationId, currentMuted) }
    }

    fun clearChat(conversationId: String) {
        viewModelScope.launch { repository.clearChatMessages(conversationId) }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (_selectedConversationId.value == conversationId) {
                selectConversation(null)
            }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch { repository.addReaction(messageId, emoji) }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch { repository.deleteMessage(messageId) }
    }

    fun start1On1Chat(contact: UserEntity, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createNew1On1Chat(contact)
            selectConversation(convId)
            onChatCreated(convId)
        }
    }

    fun createGroupChat(title: String, description: String, selectedUserIds: List<String>, onGroupCreated: (String) -> Unit) {
        viewModelScope.launch {
            val groupId = repository.createGroupChat(title, description, selectedUserIds)
            selectConversation(groupId)
            onGroupCreated(groupId)
        }
    }

    fun initiateCall(participantId: String, participantName: String, avatarUrl: String, callType: String = "AUDIO") {
        _activeCallState.value = ActiveCallState(
            isCallActive = true,
            participantName = participantName,
            participantAvatar = avatarUrl,
            callType = callType,
            durationSeconds = 0
        )

        viewModelScope.launch {
            repository.addCallLog(participantId, participantName, callType, isOutgoing = true)
        }

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCallState.value.isCallActive) {
                delay(1000)
                _activeCallState.value = _activeCallState.value.copy(
                    durationSeconds = _activeCallState.value.durationSeconds + 1
                )
            }
        }
    }

    fun toggleMuteCall() {
        _activeCallState.value = _activeCallState.value.copy(
            isMuted = !_activeCallState.value.isMuted
        )
    }

    fun toggleSpeakerCall() {
        _activeCallState.value = _activeCallState.value.copy(
            isSpeakerOn = !_activeCallState.value.isSpeakerOn
        )
    }

    fun endCall() {
        callTimerJob?.cancel()
        _activeCallState.value = ActiveCallState(isCallActive = false)
    }

    fun blockUser(userId: String, isBlocked: Boolean) {
        viewModelScope.launch { repository.blockUser(userId, isBlocked) }
    }

    fun updateUserProfile(displayName: String, username: String, bio: String) {
        viewModelScope.launch { repository.updateUserProfile(displayName, username, bio) }
    }

    fun toggleOfflineStatus() {
        val current = RealtimeChatEngine.connectionState.value
        RealtimeChatEngine.toggleOfflineMode(current != ConnectionState.OFFLINE)
    }
}

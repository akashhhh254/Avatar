package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AttachmentPickerModal
import com.example.ui.components.AvatarBadge
import com.example.ui.components.E2EShieldBanner
import com.example.ui.components.MessageBubble
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.theme.AvatarEmeraldShield
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onInitiateCall: (participantId: String, name: String, avatar: String, type: String) -> Unit,
    onOpenGroupInfo: (String) -> Unit
) {
    val conversation by viewModel.activeConversation.collectAsState()
    val recipientUser by viewModel.activeRecipientUser.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val composerText by viewModel.composerText.collectAsState()
    val replyToMessage by viewModel.replyToMessage.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val voiceRecordingSeconds by viewModel.voiceRecordingSeconds.collectAsState()
    val typingStatus by viewModel.typingStatus.collectAsState()

    var showAttachmentModal by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val activeConv = conversation ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (activeConv.isGroup) onOpenGroupInfo(activeConv.id)
                        }
                    ) {
                        AvatarBadge(
                            name = activeConv.title,
                            avatarUrl = activeConv.avatarUrl,
                            size = 38.dp,
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activeConv.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val statusText = if (typingStatus?.conversationId == activeConv.id && typingStatus?.isTyping == true) {
                                "${typingStatus?.userName} is typing..."
                            } else {
                                "Online • AVATAR Shield"
                            }
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                color = if (statusText.contains("typing")) AvatarCyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onInitiateCall(activeConv.otherUserId.ifEmpty { activeConv.id }, activeConv.title, activeConv.avatarUrl, "AUDIO")
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = AvatarCyanPrimary)
                    }
                    IconButton(onClick = {
                        onInitiateCall(activeConv.otherUserId.ifEmpty { activeConv.id }, activeConv.title, activeConv.avatarUrl, "VIDEO")
                    }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = AvatarCyanPrimary)
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("E2E Security Key Verification") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AvatarEmeraldShield) },
                            onClick = { showOverflowMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                viewModel.clearChat(activeConv.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mute Notifications") },
                            leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                viewModel.toggleMuteConversation(activeConv.id, activeConv.isMuted)
                            }
                        )

                        if (!activeConv.isGroup && recipientUser != null) {
                            val isBlocked = recipientUser?.isBlocked == true
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isBlocked) "Unblock Contact" else "Block Contact",
                                        color = if (isBlocked) AvatarCyanPrimary else MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (isBlocked) AvatarCyanPrimary else MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    if (isBlocked) {
                                        viewModel.blockUser(recipientUser!!.id, false)
                                    } else {
                                        showBlockConfirmDialog = true
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                // Reply Banner
                replyToMessage?.let { reply ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = AvatarCyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Replying to ${reply.senderName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AvatarCyanPrimary)
                                Text(reply.content, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { viewModel.replyToMessage.value = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Reply")
                            }
                        }
                    }
                }

                // Check if user is blocked
                if (recipientUser?.isBlocked == true) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Block,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You blocked this contact. Tap Unblock to send messages.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = {
                                    recipientUser?.let { viewModel.blockUser(it.id, false) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                            ) {
                                Text("Unblock", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (isVoiceRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording 0:${voiceRecordingSeconds.toString().padStart(2, '0')}",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.cancelVoiceRecording() }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.error)
                            }
                            Button(
                                onClick = { viewModel.sendVoiceMessage() },
                                colors = ButtonDefaults.buttonColors(containerColor = AvatarCyanPrimary)
                            ) {
                                Text("Send", color = Color.Black)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAttachmentModal = true }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach", tint = AvatarCyanPrimary)
                        }

                        OutlinedTextField(
                            value = composerText,
                            onValueChange = { viewModel.composerText.value = it },
                            placeholder = { Text("Message...") },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp, max = 120.dp),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        if (composerText.trim().isNotEmpty()) {
                            FloatingActionButton(
                                onClick = { viewModel.sendMessage() },
                                containerColor = AvatarCyanPrimary,
                                contentColor = Color.Black,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                            }
                        } else {
                            IconButton(onClick = { viewModel.startVoiceRecording() }) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = AvatarCyanPrimary)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            E2EShieldBanner()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMyMsg = msg.senderId == (currentUser?.id ?: "user_me")
                    MessageBubble(
                        message = msg,
                        isCurrentUser = isMyMsg,
                        onReply = { message -> viewModel.replyToMessage.value = message },
                        onReact = { message, emoji -> viewModel.addReaction(message.id, emoji) },
                        onDelete = { message -> viewModel.deleteMessage(message.id) },
                        onEdit = { message ->
                            viewModel.editingMessage.value = message
                            viewModel.composerText.value = message.content
                        }
                    )
                }
            }
        }
    }

    if (showAttachmentModal) {
        AttachmentPickerModal(
            onDismiss = { showAttachmentModal = false },
            onSelectAttachment = { type, name, size ->
                viewModel.sendMessage(
                    type = type,
                    mediaName = name,
                    mediaSize = size,
                    mediaUrl = "content://avatar/file"
                )
            }
        )
    }

    if (showBlockConfirmDialog && recipientUser != null) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Block ${recipientUser?.displayName}?")
                }
            },
            text = {
                Text("Blocked contacts will no longer be able to send you messages or call you in AVATAR. You can unblock them anytime in Settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmDialog = false
                        recipientUser?.let { viewModel.blockUser(it.id, true) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Block Contact", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

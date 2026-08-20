package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CallLogEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.UserEntity
import com.example.network.ConnectionState
import com.example.ui.components.AvatarBadge
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.theme.AvatarEmeraldShield
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainTab
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    viewModel: ChatViewModel,
    onOpenConversation: (String) -> Unit,
    onOpenNewChatGroup: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onInitiateCall: (participantId: String, name: String, avatar: String, type: String) -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeConversations by viewModel.activeConversations.collectAsState()
    val groupConversations by viewModel.groupConversations.collectAsState()
    val archivedConversations by viewModel.archivedConversations.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search messages, chats, contacts...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(24.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showSearchField = false
                                    viewModel.onSearchQueryChanged("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AvatarCyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AVATAR",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (connectionState == ConnectionState.CONNECTED_ENCRYPTED) AvatarEmeraldShield else Color.Red
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (connectionState == ConnectionState.CONNECTED_ENCRYPTED) "Encrypted" else "Offline Queue",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (!showSearchField) {
                        IconButton(onClick = { showSearchField = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { viewModel.toggleOfflineStatus() }) {
                            Icon(
                                imageVector = if (connectionState == ConnectionState.OFFLINE) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = "Network Mode",
                                tint = if (connectionState == ConnectionState.OFFLINE) Color.Red else AvatarEmeraldShield
                            )
                        }
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Chat / Group") },
                                leadingIcon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenNewChatGroup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings & Privacy") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Log Out / Switch Account", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showOverflowMenu = false
                                    showLogoutConfirmDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewChatGroup,
                containerColor = AvatarCyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Tabs Navigation
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AvatarCyanPrimary
            ) {
                Tab(
                    selected = selectedTab == MainTab.CHATS,
                    onClick = { viewModel.selectTab(MainTab.CHATS) },
                    text = { Text("Chats (${activeConversations.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == MainTab.GROUPS,
                    onClick = { viewModel.selectTab(MainTab.GROUPS) },
                    text = { Text("Groups (${groupConversations.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == MainTab.CALLS,
                    onClick = { viewModel.selectTab(MainTab.CALLS) },
                    text = { Text("Calls", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == MainTab.CONTACTS,
                    onClick = { viewModel.selectTab(MainTab.CONTACTS) },
                    text = { Text("Contacts", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content
            when (selectedTab) {
                MainTab.CHATS -> ChatsTabContent(
                    conversations = activeConversations.filter {
                        searchQuery.isEmpty() || it.title.contains(searchQuery, true) || it.lastMessageText.contains(searchQuery, true)
                    },
                    archivedCount = archivedConversations.size,
                    onConversationClick = { onOpenConversation(it.id) },
                    onTogglePin = { conv -> viewModel.togglePinConversation(conv.id, conv.isPinned) },
                    onToggleArchive = { conv -> viewModel.toggleArchiveConversation(conv.id, conv.isArchived) },
                    onToggleMute = { conv -> viewModel.toggleMuteConversation(conv.id, conv.isMuted) },
                    onDelete = { conv -> viewModel.deleteConversation(conv.id) }
                )
                MainTab.GROUPS -> GroupsTabContent(
                    groups = groupConversations.filter {
                        searchQuery.isEmpty() || it.title.contains(searchQuery, true)
                    },
                    onGroupClick = { onOpenConversation(it.id) },
                    onCreateNewGroup = onOpenNewChatGroup
                )
                MainTab.CALLS -> CallsTabContent(
                    callLogs = callLogs,
                    onInitiateCall = onInitiateCall
                )
                MainTab.CONTACTS -> ContactsTabContent(
                    contacts = contacts.filter {
                        searchQuery.isEmpty() || it.displayName.contains(searchQuery, true) || it.username.contains(searchQuery, true)
                    },
                    onStartChat = { contact ->
                        viewModel.start1On1Chat(contact) { convId -> onOpenConversation(convId) }
                    },
                    onCallContact = { contact ->
                        onInitiateCall(contact.id, contact.displayName, contact.avatarUrl, "AUDIO")
                    }
                )
            }
        }

        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of AVATAR")
                    }
                },
                text = {
                    Text("Are you sure you want to log out? You will need to verify via Phone Number or Email OTP code to log back in.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatsTabContent(
    conversations: List<ConversationEntity>,
    archivedCount: Int,
    onConversationClick: (ConversationEntity) -> Unit,
    onTogglePin: (ConversationEntity) -> Unit,
    onToggleArchive: (ConversationEntity) -> Unit,
    onToggleMute: (ConversationEntity) -> Unit,
    onDelete: (ConversationEntity) -> Unit
) {
    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No active conversations", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (archivedCount > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = "Archived", tint = AvatarCyanPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Archived Chats ($archivedCount)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    HorizontalDivider()
                }
            }

            items(conversations, key = { it.id }) { conv ->
                ConversationItemRow(
                    conversation = conv,
                    onClick = { onConversationClick(conv) },
                    onTogglePin = { onTogglePin(conv) },
                    onToggleArchive = { onToggleArchive(conv) },
                    onToggleMute = { onToggleMute(conv) },
                    onDelete = { onDelete(conv) }
                )
            }
        }
    }
}

@Composable
private fun ConversationItemRow(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(conversation.lastMessageTimestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conversation.lastMessageTimestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBadge(
            name = conversation.title,
            avatarUrl = conversation.avatarUrl,
            size = 50.dp,
            isOnline = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = conversation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = conversation.lastMessageText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Muted",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = AvatarCyanPrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AvatarCyanPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) "Unpin Chat" else "Pin Chat") },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (conversation.isMuted) "Unmute" else "Mute") },
                leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onToggleMute()
                }
            )
            DropdownMenuItem(
                text = { Text("Archive") },
                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onToggleArchive()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Chat") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun GroupsTabContent(
    groups: List<ConversationEntity>,
    onGroupClick: (ConversationEntity) -> Unit,
    onCreateNewGroup: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCreateNewGroup() },
            colors = CardDefaults.cardColors(containerColor = AvatarCyanPrimary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AvatarCyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Create New Group", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Start encrypted multi-user conversation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(groups, key = { it.id }) { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGroupClick(group) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(name = group.title, size = 48.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(group.description.ifEmpty { "E2E Encrypted Group" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CallsTabContent(
    callLogs: List<CallLogEntity>,
    onInitiateCall: (participantId: String, name: String, avatar: String, type: String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(callLogs, key = { it.id }) { call ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarBadge(name = call.participantName, avatarUrl = call.participantAvatar, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(call.participantName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (call.isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
                            contentDescription = null,
                            tint = if (call.isMissed) Color.Red else AvatarEmeraldShield,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (call.isMissed) "Missed • ${call.callType}" else "${call.durationSeconds}s • ${call.callType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { onInitiateCall(call.participantId, call.participantName, call.participantAvatar, call.callType) }) {
                    Icon(
                        imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = AvatarCyanPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactsTabContent(
    contacts: List<UserEntity>,
    onStartChat: (UserEntity) -> Unit,
    onCallContact: (UserEntity) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(contacts, key = { it.id }) { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartChat(contact) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarBadge(name = contact.displayName, avatarUrl = contact.avatarUrl, isOnline = contact.isOnline, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contact.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(contact.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                IconButton(onClick = { onCallContact(contact) }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = AvatarCyanPrimary)
                }
            }
        }
    }
}

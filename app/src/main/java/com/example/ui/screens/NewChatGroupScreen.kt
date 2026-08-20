package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.components.AvatarBadge
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatGroupScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onChatCreated: (String) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    var isCreatingGroup by remember { mutableStateOf(false) }

    var groupTitle by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    val selectedUserIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = if (isCreatingGroup) "Create Encrypted Group" else "New Chat",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isCreatingGroup) {
                        TextButton(
                            onClick = {
                                if (groupTitle.trim().isNotEmpty() && selectedUserIds.isNotEmpty()) {
                                    viewModel.createGroupChat(groupTitle, groupDescription, selectedUserIds) { convId ->
                                        onChatCreated(convId)
                                    }
                                }
                            },
                            enabled = groupTitle.trim().isNotEmpty() && selectedUserIds.isNotEmpty()
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold, color = AvatarCyanPrimary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!isCreatingGroup) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCreatingGroup = true },
                    colors = CardDefaults.cardColors(containerColor = AvatarCyanPrimary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = AvatarCyanPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("New Group Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Contact", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contacts, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.start1On1Chat(contact) { convId -> onChatCreated(convId) }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarBadge(name = contact.displayName, avatarUrl = contact.avatarUrl, isOnline = contact.isOnline)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(contact.displayName, fontWeight = FontWeight.Bold)
                                Text(contact.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = groupTitle,
                    onValueChange = { groupTitle = it },
                    label = { Text("Group Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Group Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Members (${selectedUserIds.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contacts, key = { it.id }) { contact ->
                        val isSelected = selectedUserIds.contains(contact.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedUserIds.remove(contact.id)
                                    else selectedUserIds.add(contact.id)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarBadge(name = contact.displayName, avatarUrl = contact.avatarUrl)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(contact.displayName, fontWeight = FontWeight.Bold)
                            }

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) selectedUserIds.remove(contact.id)
                                    else selectedUserIds.add(contact.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AvatarCyanPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MessageEntity
import com.example.ui.theme.AvatarCyanPrimary
import com.example.ui.theme.AvatarEmeraldShield
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: MessageEntity,
    isCurrentUser: Boolean,
    onReply: (MessageEntity) -> Unit,
    onReact: (MessageEntity, String) -> Unit,
    onDelete: (MessageEntity) -> Unit,
    onEdit: (MessageEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val backgroundColor = if (isCurrentUser) {
        AvatarCyanPrimary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentUser) Color.Black else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(backgroundColor)
                    .clickable { showMenu = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    // Sender Name if in group
                    if (!isCurrentUser && message.senderName.isNotEmpty()) {
                        Text(
                            text = message.senderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AvatarCyanPrimary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Reply preview header
                    if (message.replyToPreview.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentUser) Color.Black.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Replying: ${message.replyToPreview}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = if (isCurrentUser) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Content by type
                    if (message.isDeleted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Deleted",
                                modifier = Modifier.size(14.dp),
                                tint = textColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "This message was deleted",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        when (message.messageType) {
                            "VOICE", "AUDIO" -> {
                                VoiceNotePlayer(
                                    durationSeconds = if (message.mediaDurationSeconds > 0) message.mediaDurationSeconds else 8,
                                    isSentByMe = isCurrentUser
                                )
                            }
                            "IMAGE" -> {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Photo",
                                            tint = textColor,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                    if (message.content.isNotEmpty() && message.content != message.mediaName) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = message.content,
                                            fontSize = 14.sp,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                            "DOCUMENT" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrentUser) Color.Black.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = "Document",
                                        tint = if (isCurrentUser) Color.Black else AvatarCyanPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.mediaName.ifEmpty { "Document.pdf" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = message.mediaSize.ifEmpty { "2.1 MB" },
                                            fontSize = 11.sp,
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = message.content,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Time & Status Indicators
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp)
                    ) {
                        if (message.isEdited && !message.isDeleted) {
                            Text(
                                text = "edited • ",
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )

                        if (isCurrentUser && !message.isDeleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val icon = when (message.status) {
                                "READ" -> Icons.Default.DoneAll
                                "DELIVERED" -> Icons.Default.DoneAll
                                "SENT" -> Icons.Default.Check
                                else -> Icons.Default.Schedule
                            }
                            val tint = if (message.status == "READ") {
                                if (isCurrentUser) Color(0xFF1E3A8A) else AvatarCyanPrimary
                            } else textColor.copy(alpha = 0.7f)

                            Icon(
                                imageVector = icon,
                                contentDescription = message.status,
                                tint = tint,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Dropdown Menu on tap
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reply") },
                    leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onReply(message)
                    }
                )
                DropdownMenuItem(
                    text = { Text("React") },
                    leadingIcon = { Icon(Icons.Default.AddReaction, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        showReactionPicker = true
                    }
                )
                if (isCurrentUser && !message.isDeleted) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEdit(message)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onDelete(message)
                    }
                )
            }

            // Quick Reaction Bar
            if (showReactionPicker) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("👍", "❤️", "🔥", "😂", "😮", "🔒").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    showReactionPicker = false
                                    onReact(message, emoji)
                                }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

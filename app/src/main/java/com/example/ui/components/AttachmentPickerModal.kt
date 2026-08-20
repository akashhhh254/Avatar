package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvatarCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerModal(
    onDismiss: () -> Unit,
    onSelectAttachment: (type: String, name: String, size: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Share Content",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    color = Color(0xFFE11D48),
                    onClick = {
                        onSelectAttachment("IMAGE", "Live Photo.jpg", "2.4 MB")
                        onDismiss()
                    }
                )
                AttachmentItem(
                    icon = Icons.Default.Image,
                    label = "Gallery",
                    color = Color(0xFF2563EB),
                    onClick = {
                        onSelectAttachment("IMAGE", "Encrypted Image.png", "1.8 MB")
                        onDismiss()
                    }
                )
                AttachmentItem(
                    icon = Icons.Default.InsertDriveFile,
                    label = "Document",
                    color = Color(0xFFD97706),
                    onClick = {
                        onSelectAttachment("DOCUMENT", "Security_Report.pdf", "3.5 MB")
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    icon = Icons.Default.Mic,
                    label = "Audio",
                    color = Color(0xFF059669),
                    onClick = {
                        onSelectAttachment("AUDIO", "Voice_Recording.m4a", "850 KB")
                        onDismiss()
                    }
                )
                AttachmentItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = Color(0xFF7C3AED),
                    onClick = {
                        onSelectAttachment("LOCATION", "Current Location", "Shared GPS")
                        onDismiss()
                    }
                )
                AttachmentItem(
                    icon = Icons.Default.Person,
                    label = "Contact",
                    color = AvatarCyanPrimary,
                    onClick = {
                        onSelectAttachment("DOCUMENT", "Elena_Rostova.vcf", "12 KB")
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttachmentItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

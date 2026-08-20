package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvatarCyanPrimary

@Composable
fun VoiceNotePlayer(
    durationSeconds: Int,
    isSentByMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf("1x") }

    // Waveform bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedHeight by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "height"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Play/Pause button
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSentByMe) Color.White.copy(alpha = 0.25f) else AvatarCyanPrimary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause Voice Note",
                tint = if (isSentByMe) Color.White else Color.Black
            )
        }

        // Animated Waveform bars
        Row(
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val bars = listOf(0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 1.0f, 0.6f, 0.4f, 0.8f, 0.5f, 0.9f)
            bars.forEachIndexed { index, defaultFactor ->
                val factor = if (isPlaying) {
                    (defaultFactor * animatedHeight * ((index % 3) + 1) / 2f).coerceIn(0.2f, 1f)
                } else {
                    defaultFactor
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(factor)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isSentByMe) Color.White.copy(alpha = 0.8f) else AvatarCyanPrimary
                        )
                )
            }
        }

        // Duration / Speed pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "0:${durationSeconds.toString().padStart(2, '0')}",
                fontSize = 11.sp,
                color = if (isSentByMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSentByMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        currentSpeed = when (currentSpeed) {
                            "1x" -> "1.5x"
                            "1.5x" -> "2x"
                            else -> "1x"
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = currentSpeed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LiveControlState
import com.example.data.model.StreamStatus
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.YTRed
import kotlin.math.sin

@Composable
fun LiveVideoPreviewPlayer(
    state: LiveControlState,
    modifier: Modifier = Modifier,
    onToggleAudio: () -> Unit = {},
    onReconnectClick: () -> Unit = {}
) {
    val isStreaming = state.status == StreamStatus.LIVE || state.status == StreamStatus.RECONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "videoWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_video_preview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07080A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Background Visuals / Image fallback
            if (state.videoSourceUrl.isNotBlank() && isStreaming) {
                // Background cinematic gradient & simulated audio wave canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Ambient gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF150A10),
                                Color(0xFF090D18),
                                Color(0xFF05060A)
                            )
                        )
                    )

                    // Audio Waveform Visualizer
                    val wavePath1 = Path()
                    val wavePath2 = Path()

                    val waveCount = 50
                    for (i in 0..waveCount) {
                        val x = (i.toFloat() / waveCount) * width
                        val angle = (i * 0.25f) + phase
                        val y1 = height * 0.52f + (sin(angle.toDouble()) * (height * 0.18f)).toFloat()
                        val y2 = height * 0.52f + (sin((angle + 1.2).toDouble()) * (height * 0.14f)).toFloat()

                        if (i == 0) {
                            wavePath1.moveTo(x, y1)
                            wavePath2.moveTo(x, y2)
                        } else {
                            wavePath1.lineTo(x, y1)
                            wavePath2.lineTo(x, y2)
                        }
                    }

                    drawPath(
                        path = wavePath1,
                        brush = Brush.horizontalGradient(
                            colors = listOf(YTRed.copy(alpha = 0.8f), CyberBlue.copy(alpha = 0.8f))
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    drawPath(
                        path = wavePath2,
                        brush = Brush.horizontalGradient(
                            colors = listOf(CyberBlue.copy(alpha = 0.6f), LiveGreen.copy(alpha = 0.6f))
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Subtle CRT scan line
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, scanLineY * height),
                        end = Offset(width, scanLineY * height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            } else {
                // Idle monitor state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF10121A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Camera Idle",
                            tint = Color(0xFF4A4E63),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RTMPS ENCODER STANDBY",
                            color = Color(0xFF6B728E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Top Overlay Bar (LIVE Badge, Duration, Connection Quality)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.status == StreamStatus.LIVE) {
                        LivePulseBadge(customText = "LIVE")
                    } else if (state.status == StreamStatus.RECONNECTING) {
                        Box(
                            modifier = Modifier
                                .background(WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "RECONNECTING (${state.reconnectAttempts})",
                                color = WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LivePulseBadge(isLive = false, customText = state.status.name)
                    }

                    if (isStreaming) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatSeconds(state.elapsedSeconds),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Ingest / Resolution Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = state.metrics.resolution,
                            color = CyberBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${state.metrics.bitrateKbps} kbps",
                            color = LiveGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Bottom Audio VU & Controls Overlay
            if (isStreaming) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio Level Meter (Stereo VU bars)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic Status",
                            tint = if (state.isMuted) YTRed else LiveGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Stereo VU Bars
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(16) { index ->
                                    val barColor = when {
                                        state.isMuted -> Color(0xFF333333)
                                        index < 10 -> LiveGreen
                                        index < 14 -> WarningAmber
                                        else -> YTRed
                                    }
                                    val active = !state.isMuted && ((index + (phase * 3).toInt()) % 16 > 4)
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(4.dp)
                                            .background(
                                                if (active) barColor else barColor.copy(alpha = 0.2f),
                                                RoundedCornerShape(1.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Interactive quick buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleAudio,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isMuted) Icons.Default.VolumeUp else Icons.Default.MicOff,
                                contentDescription = "Toggle Audio",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onReconnectClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reconnect",
                                tint = CyberBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

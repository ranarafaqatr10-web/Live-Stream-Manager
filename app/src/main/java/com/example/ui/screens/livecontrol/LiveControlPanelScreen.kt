package com.example.ui.screens.livecontrol

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.YouTubeChannelEntity
import com.example.data.model.LiveControlState
import com.example.data.model.StreamStatus
import com.example.ui.components.EncoderMetricsCard
import com.example.ui.components.LivePulseBadge
import com.example.ui.components.LiveVideoPreviewPlayer
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.YTRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveControlPanelScreen(
    viewModel: MainViewModel,
    state: LiveControlState,
    channel: YouTubeChannelEntity?,
    onNavigateToCreate: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isLive = state.status == StreamStatus.LIVE || state.status == StreamStatus.RECONNECTING
    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val now = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("live_control_panel_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Live Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Podcasts,
                    contentDescription = null,
                    tint = if (isLive) YTRed else CyberBlue,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "LIVE CONTROL STUDIO",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isLive) "Broadcasting to ${state.channelTitle}" else "Standby Mode",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (isLive) {
                LivePulseBadge(customText = "LIVE")
            } else {
                LivePulseBadge(isLive = false, customText = "OFFLINE")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Live Video Preview Canvas
        LiveVideoPreviewPlayer(
            state = state,
            onToggleAudio = { viewModel.toggleAudioMute() },
            onReconnectClick = { viewModel.triggerSimulatedReconnect() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Stream Status & Primary Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STREAM STATUS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = state.status.name,
                            color = when (state.status) {
                                StreamStatus.LIVE -> LiveGreen
                                StreamStatus.CONNECTING -> CyberBlue
                                StreamStatus.RECONNECTING -> WarningAmber
                                else -> TextSecondary
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LIVE DURATION",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = MainViewModel.formatDuration(state.elapsedSeconds),
                            color = if (isLive) YTRed else TextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: START / STOP / RECONNECT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isLive) {
                        Button(
                            onClick = { viewModel.stopLiveStream() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("stop_live_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = YTRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STOP LIVE STREAM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.triggerSimulatedReconnect() },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(48.dp)
                                .testTag("force_reconnect_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CyberBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reconnect", color = TextPrimary, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onNavigateToCreate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_new_stream_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = YTRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START NEW LIVE STREAM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (isLive && state.youtubeBroadcastUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.youtubeBroadcastUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("open_live_yt_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = YTRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Live Stream on YouTube", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time Encoder Telemetry
        EncoderMetricsCard(
            metrics = state.metrics,
            connectionQuality = state.connectionQuality
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Technical Broadcast Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BROADCAST SPECIFICATIONS",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                SpecRow(label = "Stream Title", value = state.streamTitle.ifBlank { "Standby" })
                SpecRow(label = "Target YouTube Channel", value = channel?.channelTitle ?: "Not Connected")
                SpecRow(label = "RTMPS Ingest Endpoint", value = channel?.ingestUrl ?: "rtmps://a.rtmps.youtube.com/live2")
                SpecRow(label = "Stream Key", value = channel?.streamKeyMasked ?: "••••-••••-••••-9821")
                SpecRow(label = "Video Source", value = if (state.videoSourceUrl.isNotBlank()) state.videoSourceUrl.take(36) + "..." else "Standby")
                SpecRow(label = "Auto Reconnect Policy", value = "Exponential Backoff (Max 5 retries)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

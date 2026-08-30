package com.example.ui.screens.createlive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.YouTubeChannelEntity
import com.example.data.model.SampleVideoCatalog
import com.example.data.model.VideoSourcePreset
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLiveScreen(
    viewModel: MainViewModel,
    channel: YouTubeChannelEntity?,
    onStreamStarted: (streamId: String) -> Unit,
    onNavigateToChannel: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var videoUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    var title by remember { mutableStateOf("🔴 24/7 Lo-Fi Chill Beats | Relax & Code Stream") }
    var description by remember { mutableStateOf("High-quality live stream broadcast powered by YT Live Manager.") }
    var thumbnailUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600") }

    var selectedCategory by remember { mutableStateOf(SampleVideoCatalog.categories[1]) } // Music
    var selectedPrivacy by remember { mutableStateOf(SampleVideoCatalog.privacyOptions[0]) } // Public
    var selectedResolution by remember { mutableStateOf(SampleVideoCatalog.resolutionPresets[0]) } // 1080p60
    var durationMinutes by remember { mutableIntStateOf(120) }
    var isLooping by remember { mutableStateOf(true) }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isPrivacyExpanded by remember { mutableStateOf(false) }
    var isResolutionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("create_live_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = YTRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CREATE LIVE STREAM",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Configure authorized video source & broadcast to your YouTube channel",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning if channel is not connected
        if (channel == null || !channel.isConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221414)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(YTRed))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ YouTube Channel not connected! Connect now to get your RTMPS stream key.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onNavigateToChannel,
                        colors = ButtonDefaults.buttonColors(containerColor = YTRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect", fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Preset Video Selector
        Text(
            text = "Select Authorized Video Preset or Paste URL",
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SampleVideoCatalog.presets) { preset ->
                val isSelected = videoUrl == preset.url
                Card(
                    modifier = Modifier
                        .width(170.dp)
                        .clickable {
                            videoUrl = preset.url
                            title = preset.defaultTitle
                            description = preset.defaultDescription
                            thumbnailUrl = preset.thumbnailUrl
                            selectedCategory = preset.category
                        }
                        .testTag("preset_${preset.category}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) YTRed else DarkCardBorder
                        )
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        ) {
                            AsyncImage(
                                model = preset.thumbnailUrl,
                                contentDescription = preset.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(YTRed, RoundedCornerShape(bottomStart = 6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = preset.title,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${preset.category} • ${preset.durationText}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Video URL Field with Quick Paste
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video Source URL (MP4, HLS, RTSP)") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = CyberBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val text = clipboardManager.getText()?.text
                            if (!text.isNullOrBlank()) {
                                videoUrl = text
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste URL",
                                tint = CyberBlue
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_video_url_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YTRed,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stream Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream Title") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YTRed,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Tags") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary)
                    },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_description_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YTRed,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Thumbnail URL
                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    label = { Text("Thumbnail Image URL") },
                    leadingIcon = {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YTRed,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category & Privacy Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCategoryExpanded,
                        onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YTRed,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            SampleVideoCatalog.categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = cat
                                        isCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Privacy Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isPrivacyExpanded,
                        onExpandedChange = { isPrivacyExpanded = !isPrivacyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPrivacy,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Privacy") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPrivacyExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YTRed,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isPrivacyExpanded,
                            onDismissRequest = { isPrivacyExpanded = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            SampleVideoCatalog.privacyOptions.forEach { priv ->
                                DropdownMenuItem(
                                    text = { Text(priv, color = TextPrimary) },
                                    onClick = {
                                        selectedPrivacy = priv
                                        isPrivacyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Encoder Resolution Preset Dropdown
                ExposedDropdownMenuBox(
                    expanded = isResolutionExpanded,
                    onExpandedChange = { isResolutionExpanded = !isResolutionExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedResolution,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Encoder Preset / Bitrate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isResolutionExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YTRed,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isResolutionExpanded,
                        onDismissRequest = { isResolutionExpanded = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        SampleVideoCatalog.resolutionPresets.forEach { res ->
                            DropdownMenuItem(
                                text = { Text(res, color = TextPrimary) },
                                onClick = {
                                    selectedResolution = res
                                    isResolutionExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Duration Selector Chips
                Text(
                    text = "Scheduled Stream Duration",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h", 480 to "8h", 1440 to "24h").forEach { (min, label) ->
                        val isSelected = durationMinutes == min
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) YTRed else DarkSurfaceVariant)
                                .clickable { durationMinutes = min }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Loop Video Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1016), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = CyberBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Loop Video Continuously",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Auto-rewind and stream 24/7 without frame drop",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isLooping,
                        onCheckedChange = { isLooping = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = YTRed,
                            uncheckedTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.testTag("loop_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Start Button
                Button(
                    onClick = {
                        viewModel.createAndStartLiveStream(
                            title = title,
                            description = description,
                            videoUrl = videoUrl,
                            thumbnailUrl = thumbnailUrl,
                            category = selectedCategory,
                            privacy = selectedPrivacy,
                            durationMinutes = durationMinutes,
                            isLooping = isLooping,
                            resolutionPreset = selectedResolution,
                            startImmediately = true,
                            onSuccess = onStreamStarted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_live_broadcast_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YTRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START LIVE STREAM NOW",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Schedule Button
                OutlinedButton(
                    onClick = {
                        viewModel.createAndStartLiveStream(
                            title = title,
                            description = description,
                            videoUrl = videoUrl,
                            thumbnailUrl = thumbnailUrl,
                            category = selectedCategory,
                            privacy = selectedPrivacy,
                            durationMinutes = durationMinutes,
                            isLooping = isLooping,
                            resolutionPreset = selectedResolution,
                            startImmediately = false,
                            onSuccess = onStreamStarted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("schedule_stream_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = CyberBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Schedule for Later",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

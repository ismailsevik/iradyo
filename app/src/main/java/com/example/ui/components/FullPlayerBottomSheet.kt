package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PlayerState
import com.example.ui.theme.GoogleBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerBottomSheet(
    playerState: PlayerState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward30: () -> Unit,
    onSeekTo: (seconds: Int) -> Unit,
    onSetPlaybackSpeed: (speed: Float) -> Unit,
    onToggleTrimSilence: () -> Unit,
    onSetSleepTimer: (minutes: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val episode = playerState.currentEpisode ?: return
    BackHandler { onDismiss() }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }

    var sliderPosition by remember(playerState.currentPositionSeconds) {
        mutableFloatStateOf(playerState.currentPositionSeconds.toFloat())
    }
    var isDraggingSlider by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_player_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(44.dp).testTag("close_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = episode.podcastTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { showDescriptionDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Show notes",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Artwork
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(episode.podcastImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Episode Title
            Text(
                text = episode.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${episode.podcastTitle} • ${episode.releaseDate}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Slider progress bar
            val totalSec = playerState.durationSeconds.coerceAtLeast(1)
            Slider(
                value = if (isDraggingSlider) sliderPosition else playerState.currentPositionSeconds.toFloat(),
                onValueChange = {
                    isDraggingSlider = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    onSeekTo(sliderPosition.toInt())
                },
                valueRange = 0f..totalSec.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = GoogleBlue,
                    activeTrackColor = GoogleBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_progress_slider")
            )

            // Time stamps: elapsed and remaining
            val currentSec = if (isDraggingSlider) sliderPosition.toInt() else playerState.currentPositionSeconds
            val remainingSec = (totalSec - currentSec).coerceAtLeast(0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentSec),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-${formatTime(remainingSec)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Controls: -10s, Big Play/Pause, +30s
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind 10s
                IconButton(
                    onClick = onRewind10,
                    modifier = Modifier.size(56.dp).testTag("full_player_rewind10")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Play / Pause Circle FAB
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(GoogleBlue)
                        .clickable { onTogglePlayPause() }
                        .shadow(8.dp, CircleShape)
                        .testTag("full_player_play_pause")
                ) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(30.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Fast Forward 30s
                IconButton(
                    onClick = onForward30,
                    modifier = Modifier.size(56.dp).testTag("full_player_forward30")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward30,
                        contentDescription = "Forward 30s",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Secondary Audio Tools row (Google Podcasts signature)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback speed pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showSpeedDialog = true }
                        .testTag("speed_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Playback speed",
                            tint = GoogleBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Trim silence toggle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (playerState.isTrimSilence) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleTrimSilence() }
                        .testTag("trim_silence_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeOff,
                            contentDescription = "Trim silence",
                            tint = if (playerState.isTrimSilence) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sessizliği Kırp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (playerState.isTrimSilence) GoogleBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Sleep Timer pill
                val sleepRemaining = playerState.sleepTimerMinutesRemaining
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (sleepRemaining != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showSleepTimerDialog = true }
                        .testTag("sleep_timer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep timer",
                            tint = if (sleepRemaining != null) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sleepRemaining != null) "${sleepRemaining} dk" else "Uyku",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sleepRemaining != null) GoogleBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show notes quick card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { showDescriptionDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bölüm Notları ve Açıklama",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = episode.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Speed Selector Dialog
    if (showSpeedDialog) {
        SpeedSelectionModal(
            currentSpeed = playerState.playbackSpeed,
            onSpeedSelected = {
                onSetPlaybackSpeed(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerModal(
            currentRemaining = playerState.sleepTimerMinutesRemaining,
            onMinutesSelected = {
                onSetSleepTimer(it)
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Description Dialog
    if (showDescriptionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = {
                Text(episode.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Yayın Tarihi: ${episode.releaseDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = episode.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showDescriptionDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
fun SpeedSelectionModal(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 1.8f, 2.0f)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Oynatma Hızı", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeedSelected(speed) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${speed}x" + if (speed == 1.0f) " (Normal)" else "",
                            fontSize = 15.sp,
                            fontWeight = if (currentSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentSpeed == speed) GoogleBlue else MaterialTheme.colorScheme.onSurface
                        )
                        if (currentSpeed == speed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = GoogleBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
fun SleepTimerModal(
    currentRemaining: Int?,
    onMinutesSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Pair("Kapat", null),
        Pair("5 dakika", 5),
        Pair("15 dakika", 15),
        Pair("30 dakika", 30),
        Pair("45 dakika", 45),
        Pair("60 dakika (1 saat)", 60)
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Uyku Zamanlayıcısı", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                options.forEach { (label, minutes) ->
                    val isSelected = currentRemaining == minutes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMinutesSelected(minutes) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = GoogleBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

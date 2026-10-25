package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PlayerState
import com.example.ui.theme.GoogleBlue

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onExpandClicked: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward30: () -> Unit,
    modifier: Modifier = Modifier
) {
    val episode = playerState.currentEpisode ?: return

    val progress = if (playerState.durationSeconds > 0) {
        playerState.currentPositionSeconds.toFloat() / playerState.durationSeconds.toFloat()
    } else 0f

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clickable { onExpandClicked() }
            .testTag("mini_player_bar")
    ) {
        Column {
            // Linear progress indicator on top edge
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = GoogleBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork thumbnail
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(episode.podcastImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Podcast name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = episode.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = episode.podcastTitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Rewind 10s
                IconButton(
                    onClick = onRewind10,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play / Pause / Buffering button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoogleBlue)
                        .clickable { onTogglePlayPause() }
                        .testTag("mini_player_play_pause")
                ) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Forward 30s
                IconButton(
                    onClick = onForward30,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward30,
                        contentDescription = "Forward 30s",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

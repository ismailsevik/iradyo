package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Episode
import com.example.ui.theme.GoogleBlue

@Composable
fun EpisodeCard(
    episode: Episode,
    isPlaying: Boolean,
    onPlayClicked: () -> Unit,
    onQueueClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onCardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClicked() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("episode_card_${episode.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Episode cover
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(episode.podcastImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Date & podcast title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = episode.releaseDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = episode.podcastTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = episode.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description snippet
                Text(
                    text = episode.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action row with Google Podcasts pill play button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Google Podcasts iconic pill play button
            val hasProgress = episode.playbackPositionSeconds > 0 && !episode.isPlayed
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onPlayClicked() }
                    .testTag("play_button_${episode.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else GoogleBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasProgress) episode.remainingFormatted else episode.durationFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick actions: Queue, Download, Favorite, Share
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Add to Queue
                IconButton(
                    onClick = onQueueClicked,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (episode.isInQueue) Icons.Default.PlaylistAddCheck else Icons.Default.PlaylistAdd,
                        contentDescription = "Queue",
                        tint = if (episode.isInQueue) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Download
                IconButton(
                    onClick = onDownloadClicked,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (episode.isDownloaded) Icons.Default.Check else Icons.Outlined.Download,
                        contentDescription = "Download",
                        tint = if (episode.isDownloaded) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Favorite
                IconButton(
                    onClick = onFavoriteClicked,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (episode.isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (episode.isFavorited) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            thickness = 0.5.dp
        )
    }
}

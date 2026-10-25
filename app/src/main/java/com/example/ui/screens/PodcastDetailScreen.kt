package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.ui.components.EpisodeCard
import com.example.ui.theme.GoogleBlue

@Composable
fun PodcastDetailScreen(
    podcast: Podcast,
    episodes: List<Episode>,
    currentPlayingEpisodeId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onToggleQueue: (Episode) -> Unit,
    onToggleDownload: (Episode) -> Unit,
    onToggleFavoriteEpisode: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("podcast_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Top App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (podcast.isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (podcast.isFavorited) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Podcast Header Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(podcast.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = podcast.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = podcast.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = podcast.author,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = podcast.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoogleBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${podcast.rating} (${podcast.subscriberCount} abone)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subscribe button
                    if (podcast.isSubscribed) {
                        OutlinedButton(
                            onClick = onToggleSubscribe,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("detail_subscribe_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GoogleBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abone Olundu", color = GoogleBlue, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onToggleSubscribe,
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("detail_subscribe_button")
                        ) {
                            Text("Abone Ol", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description text
                    Text(
                        text = podcast.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isDescriptionExpanded) 20 else 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp,
                        modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    )

                    Text(
                        text = if (isDescriptionExpanded) "Daha az göster" else "Daha fazla göster...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoogleBlue,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }

            // Episode List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bölümler (${episodes.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Episodes
            val podcastEpisodes = episodes.filter { it.podcastId == podcast.id }
            if (podcastEpisodes.isEmpty()) {
                item {
                    Text(
                        text = "Bu podcast için henüz bölüm bulunmuyor.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            } else {
                items(podcastEpisodes, key = { it.id }) { episode ->
                    EpisodeCard(
                        episode = episode,
                        isPlaying = isPlaying && currentPlayingEpisodeId == episode.id,
                        onPlayClicked = { onPlayEpisode(episode) },
                        onQueueClicked = { onToggleQueue(episode) },
                        onDownloadClicked = { onToggleDownload(episode) },
                        onFavoriteClicked = { onToggleFavoriteEpisode(episode) },
                        onCardClicked = { onPlayEpisode(episode) }
                    )
                }
            }
        }
    }
}

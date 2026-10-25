package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.model.Podcast
import com.example.ui.components.EpisodeCard
import com.example.ui.components.PodcastCard
import com.example.ui.theme.GoogleBlue

@Composable
fun ActivityScreen(
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    queuedEpisodes: List<Episode>,
    downloadedEpisodes: List<Episode>,
    historyEpisodes: List<Episode>,
    subscribedPodcasts: List<Podcast>,
    currentPlayingEpisodeId: String?,
    isPlaying: Boolean,
    onPlayEpisode: (Episode) -> Unit,
    onToggleQueue: (Episode) -> Unit,
    onToggleDownload: (Episode) -> Unit,
    onToggleFavorite: (Episode) -> Unit,
    onPodcastClicked: (Podcast) -> Unit,
    onToggleSubscribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Kuyruk", "İndirilenler", "Geçmiş", "Abonelikler")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("activity_screen")
    ) {
        // Sub tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoogleBlue
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { onSelectSubTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("activity_subtab_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> {
                // Queue Tab
                val totalMins = queuedEpisodes.sumOf { it.durationSeconds } / 60
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${queuedEpisodes.size} Bölüm Sırada",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (totalMins > 0) {
                                Text(
                                    text = "Toplam $totalMins dk",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (queuedEpisodes.isEmpty()) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.QueueMusic,
                                title = "Kuyruğunuz Boş",
                                subtitle = "Bölüm kartlarındaki '+' simgesine dokunarak daha sonra dinlemek için kuyruğa ekleyebilirsiniz."
                            )
                        }
                    } else {
                        items(queuedEpisodes, key = { "queue_${it.id}" }) { episode ->
                            EpisodeCard(
                                episode = episode,
                                isPlaying = isPlaying && currentPlayingEpisodeId == episode.id,
                                onPlayClicked = { onPlayEpisode(episode) },
                                onQueueClicked = { onToggleQueue(episode) },
                                onDownloadClicked = { onToggleDownload(episode) },
                                onFavoriteClicked = { onToggleFavorite(episode) },
                                onCardClicked = { onPlayEpisode(episode) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Downloads Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Çevrimdışı İndirilenler",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${downloadedEpisodes.size} Bölüm",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (downloadedEpisodes.isEmpty()) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.DownloadDone,
                                title = "İndirilen Bölüm Yok",
                                subtitle = "İnternetiniz yokken dinlemek için istediğiniz podcast bölümlerini indirebilirsiniz."
                            )
                        }
                    } else {
                        items(downloadedEpisodes, key = { "download_${it.id}" }) { episode ->
                            EpisodeCard(
                                episode = episode,
                                isPlaying = isPlaying && currentPlayingEpisodeId == episode.id,
                                onPlayClicked = { onPlayEpisode(episode) },
                                onQueueClicked = { onToggleQueue(episode) },
                                onDownloadClicked = { onToggleDownload(episode) },
                                onFavoriteClicked = { onToggleFavorite(episode) },
                                onCardClicked = { onPlayEpisode(episode) }
                            )
                        }
                    }
                }
            }
            2 -> {
                // History Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Text(
                            text = "Son Dinlenenler",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (historyEpisodes.isEmpty()) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.History,
                                title = "Dinleme Geçmişi Boş",
                                subtitle = "Dinlediğiniz podcast bölümleri kaldığınız yerden devam etmeniz için burada listelenir."
                            )
                        }
                    } else {
                        items(historyEpisodes, key = { "history_${it.id}" }) { episode ->
                            EpisodeCard(
                                episode = episode,
                                isPlaying = isPlaying && currentPlayingEpisodeId == episode.id,
                                onPlayClicked = { onPlayEpisode(episode) },
                                onQueueClicked = { onToggleQueue(episode) },
                                onDownloadClicked = { onToggleDownload(episode) },
                                onFavoriteClicked = { onToggleFavorite(episode) },
                                onCardClicked = { onPlayEpisode(episode) }
                            )
                        }
                    }
                }
            }
            3 -> {
                // Subscriptions Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Text(
                            text = "Abone Olunan Kanallar (${subscribedPodcasts.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (subscribedPodcasts.isEmpty()) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.Podcasts,
                                title = "Aboneliğiniz Bulunmuyor",
                                subtitle = "'Keşfet' bölümünden beğendiğiniz podcast'lere abone olabilirsiniz."
                            )
                        }
                    } else {
                        items(subscribedPodcasts, key = { "sub_${it.id}" }) { podcast ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPodcastClicked(podcast) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(podcast.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = podcast.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = podcast.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${podcast.author} • ${podcast.totalEpisodes} Bölüm",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { onToggleSubscribe(podcast.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Bildirimler açık",
                                        tint = GoogleBlue
                                    )
                                }
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateNotice(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

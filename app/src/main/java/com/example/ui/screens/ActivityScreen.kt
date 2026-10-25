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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.GoogleBlue

@Composable
fun ActivityScreen(
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    queuedEpisodes: List<Episode>,
    downloadedEpisodes: List<Episode>,
    favoriteEpisodes: List<Episode>,
    favoritePodcasts: List<Podcast>,
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
    onToggleFavoritePodcast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Kuyruk", "İndirilenler", "Favoriler", "Geçmiş", "Abonelikler")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("activity_screen")
    ) {
        // Sub tabs
        TabRow(
            selectedTabIndex = selectedSubTab.coerceIn(0, tabTitles.size - 1),
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
                            fontSize = 12.sp,
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
                                title = "İndirilmiş Bölüm Yok",
                                subtitle = "İnternet bağlantınız olmadığında dinlemek için bölümlerdeki indirme simgesine dokunun."
                            )
                        }
                    } else {
                        items(downloadedEpisodes, key = { "downloaded_${it.id}" }) { episode ->
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
                // Favorites Tab (Both podcasts and episodes)
                val totalFavorites = favoriteEpisodes.size + favoritePodcasts.size
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
                                text = "Favorilerim & Kaydedilenler",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalFavorites İçerik",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (totalFavorites == 0) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.Favorite,
                                title = "Henüz Favori Eklenmedi",
                                subtitle = "Beğendiğiniz podcast ve bölümleri kalp simgesine dokunarak favorilerinize ekleyin. Eklenen içerikler Google hesabınıza otomatik kaydedilir."
                            )
                        }
                    } else {
                        // Favorited Podcasts
                        if (favoritePodcasts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Beğenilen Podcast Kanalları (${favoritePodcasts.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoogleBlue,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            items(favoritePodcasts, key = { "fav_pod_${it.id}" }) { podcast ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPodcastClicked(podcast) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
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
                                            text = "${podcast.author} • ${podcast.category}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { onToggleFavoritePodcast(podcast.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favoriden çıkar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    thickness = 0.5.dp
                                )
                            }
                            item { Spacer(modifier = Modifier.height(14.dp)) }
                        }

                        // Favorited Episodes
                        if (favoriteEpisodes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Beğenilen Podcast Bölümleri (${favoriteEpisodes.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoogleBlue,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            items(favoriteEpisodes, key = { "fav_ep_${it.id}" }) { episode ->
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
            }
            3 -> {
                // History Tab
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
                                text = "Dinleme Geçmişi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${historyEpisodes.size} Kayıt",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (historyEpisodes.isEmpty()) {
                        item {
                            EmptyStateNotice(
                                icon = Icons.Default.History,
                                title = "Dinleme Geçmişi Boş",
                                subtitle = "Dinlediğiniz bölümler burada saklanır ve kaldığınız yerden devam edebilirsiniz."
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
            4 -> {
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

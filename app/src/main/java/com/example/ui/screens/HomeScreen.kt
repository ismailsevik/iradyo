package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.ui.components.EpisodeCard
import com.example.ui.components.SubscriptionAvatarItem
import com.example.ui.theme.GoogleBlue

@Composable
fun HomeScreen(
    subscribedPodcasts: List<Podcast>,
    episodes: List<Episode>,
    currentPlayingEpisodeId: String?,
    isPlaying: Boolean,
    selectedFilterChip: Int,
    onSelectFilterChip: (Int) -> Unit,
    onPodcastClicked: (Podcast) -> Unit,
    onNavigateToExplore: () -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onToggleQueue: (Episode) -> Unit,
    onToggleDownload: (Episode) -> Unit,
    onToggleFavorite: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Subscriptions Section
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Abonelikleriniz",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tümünü Gör",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoogleBlue,
                        modifier = Modifier.clickable { onNavigateToExplore() }
                    )
                }

                if (subscribedPodcasts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToExplore() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Podcasts,
                                contentDescription = null,
                                tint = GoogleBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Henüz bir podcast'e abone olmadınız",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Arama ve Keşfet ekranından dilediğiniz yayınlara abone olabilirsiniz.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(subscribedPodcasts, key = { it.id }) { podcast ->
                            SubscriptionAvatarItem(
                                podcast = podcast,
                                onClick = { onPodcastClicked(podcast) }
                            )
                        }

                        // "+ Keşfet" pill item at the end of carousel
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(68.dp)
                                    .clickable { onNavigateToExplore() }
                                    .testTag("home_explore_more_button")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Keşfet",
                                        tint = GoogleBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Keşfet",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoogleBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter chips (Google Podcasts style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("Tümü", "İndirilenler", "Devam Edenler")
                chips.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedFilterChip == index,
                        onClick = { onSelectFilterChip(index) },
                        label = { Text(title, fontSize = 13.sp) },
                        leadingIcon = if (selectedFilterChip == index) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_$index")
                    )
                }
            }
        }

        // Section Title: "Yeni Bölümler"
        item {
            Text(
                text = "Yeni Bölümler",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Episodes Feed
        if (episodes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu filtreye uygun bölüm bulunamadı.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(episodes, key = { it.id }) { episode ->
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

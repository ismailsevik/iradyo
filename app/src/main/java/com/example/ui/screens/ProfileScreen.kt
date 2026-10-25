package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.UserProfile
import com.example.ui.components.EpisodeCard
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow

@Composable
fun ProfileScreen(
    currentUser: UserProfile?,
    favoritePodcasts: List<Podcast>,
    listeningHistory: List<Episode>,
    subscribedCount: Int,
    currentPlayingEpisodeId: String?,
    isPlaying: Boolean,
    onPlayEpisode: (Episode) -> Unit,
    onToggleQueue: (Episode) -> Unit,
    onToggleDownload: (Episode) -> Unit,
    onToggleFavoriteEpisode: (Episode) -> Unit,
    onPodcastClicked: (Podcast) -> Unit,
    onToggleFavoritePodcast: (String) -> Unit,
    onOpenAuthDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileSubTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History, 2: Settings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // User Profile Header Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarColor = try {
                        Color(android.graphics.Color.parseColor(currentUser?.avatarColorHex ?: "#1A73E8"))
                    } catch (_: Exception) {
                        GoogleBlue
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                    ) {
                        Text(
                            text = currentUser?.fullName?.take(1)?.uppercase() ?: "İ",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.fullName ?: "Misafir Kullanıcı",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = currentUser?.email ?: "ismailseviktr@gmail.com",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account switch / manage button
                    OutlinedButton(
                        onClick = onOpenAuthDialog,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("manage_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hesap Değiştir / Giriş Yap", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats row: Listening time, Favorites, Subscriptions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val hours = (currentUser?.totalListeningMinutes ?: 0) / 60
                        val mins = (currentUser?.totalListeningMinutes ?: 0) % 60
                        val timeStr = if (hours > 0) "${hours}s ${mins}d" else "${mins} dk"

                        StatItem(
                            icon = Icons.Default.Headphones,
                            color = GoogleBlue,
                            value = timeStr,
                            label = "Dinleme Süresi"
                        )
                        StatItem(
                            icon = Icons.Default.Favorite,
                            color = GoogleRed,
                            value = "${favoritePodcasts.size}",
                            label = "Beğenilen"
                        )
                        StatItem(
                            icon = Icons.Default.Podcasts,
                            color = GoogleGreen,
                            value = "$subscribedCount",
                            label = "Abone Olunan"
                        )
                    }
                }
            }
        }

        // Sub Tabs: Beğenilenler, Dinleme Geçmişi, Ayarlar
        item {
            val tabs = listOf("Beğenilen Podcast'ler", "Dinleme Geçmişi", "Ayarlar")
            TabRow(
                selectedTabIndex = profileSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoogleBlue,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = profileSubTab == index,
                        onClick = { profileSubTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (profileSubTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("profile_tab_$index")
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (profileSubTab) {
            0 -> {
                // Favorites Tab (Beğenilenler)
                if (favoritePodcasts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Henüz beğendiğiniz podcast yok",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Keşfet veya Ana Sayfadaki kalp simgesine dokunarak podcast'leri favorilerinize ekleyebilirsiniz.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(favoritePodcasts, key = { "fav_${it.id}" }) { podcast ->
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
                                    .size(56.dp)
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
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
            1 -> {
                // Listening History Tab
                if (listeningHistory.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Dinleme geçmişiniz henüz oluşmadı",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Dinlediğiniz bölümler burada saklanır ve kaldığınız yerden devam edebilirsiniz.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(listeningHistory, key = { "profile_hist_${it.id}" }) { episode ->
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
            2 -> {
                // Settings Tab
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingItem(
                            icon = Icons.Default.Speed,
                            title = "Varsayılan Oynatma Hızı",
                            subtitle = "1.0x (Normal Hız)"
                        )
                        SettingItem(
                            icon = Icons.Default.Download,
                            title = "Otomatik İndirme",
                            subtitle = "Yalnızca Wi-Fi üzerinden yeni bölümleri indir"
                        )
                        SettingItem(
                            icon = Icons.Default.ManageAccounts,
                            title = "Hesap ve Gizlilik",
                            subtitle = "Google Podcasts senkronizasyon protokolü"
                        )
                        SettingItem(
                            icon = Icons.Outlined.Info,
                            title = "Uygulama Sürümü",
                            subtitle = "iRadyo v1.0.0 (Google Podcast'ler Mimarisi)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoogleBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        thickness = 0.5.dp
    )
}

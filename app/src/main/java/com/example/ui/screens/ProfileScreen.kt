package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    currentUser: UserProfile?,
    firebaseUser: FirebaseUser?,
    favoritePodcasts: List<Podcast>,
    listeningHistory: List<Episode>,
    subscribedCount: Int,
    currentPlayingEpisodeId: String?,
    isPlaying: Boolean,
    isSupporter: Boolean,
    purchaseMessage: String?,
    appThemeMode: Int,
    autoDownload: Boolean,
    wifiOnlyDownload: Boolean,
    onPlayEpisode: (Episode) -> Unit,
    onToggleQueue: (Episode) -> Unit,
    onToggleDownload: (Episode) -> Unit,
    onToggleFavoriteEpisode: (Episode) -> Unit,
    onPodcastClicked: (Podcast) -> Unit,
    onToggleFavoritePodcast: (String) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onBuyDeveloperSupport: (Activity) -> Unit,
    onSimulateSupport: () -> Unit,
    onClearPurchaseMessage: () -> Unit,
    onSetThemeMode: (Int) -> Unit,
    onToggleAutoDownload: () -> Unit,
    onToggleWifiOnly: () -> Unit,
    onClearHistory: () -> Unit,
    onClearDownloads: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onDeleteAccountAndData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileSubTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History, 2: Settings
    val context = LocalContext.current

    // Dialog states for Settings
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearDownloadsDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val displayName = firebaseUser?.displayName
        ?: currentUser?.fullName
        ?: "Misafir Kullanıcı"

    val displayEmail = firebaseUser?.email
        ?: currentUser?.email
        ?: "Giriş Yapılmadı (Yerel Kullanım)"

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

                    Box {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(avatarColor)
                        ) {
                            Text(
                                text = displayName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Gold Supporter Badge overlay
                        if (isSupporter) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(GoogleYellow)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Destekçi Rozeti",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSupporter) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoogleYellow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🏅 Destekçi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB06000),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = displayEmail,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Cloud Sync Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (firebaseUser != null) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (firebaseUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (firebaseUser != null) GoogleGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (firebaseUser != null) "Google Hesabı: Bulut Kaydı Aktif" else "Yerel Depolama (Google ile bağlanın)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (firebaseUser != null) Color(0xFF137333) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                        Text(
                            text = if (firebaseUser != null) "Hesap Yönetimi" else "Google ile Giriş Yap",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
            val tabs = listOf("Beğenilenler", "Dinleme Geçmişi", "Ayarlar")
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
                // Favorites Tab
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
                                text = "Keşfet veya Ana Sayfadaki kalp simgesine dokunarak podcast'leri favorilerinize ekleyebilirsiniz. Eklediğiniz podcast'ler Google hesabınıza otomatik kaydedilir.",
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
                                text = "Dinleme geçmişiniz boş",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Dinlediğiniz bölümler burada saklanır ve kaldığınız yerden dinlemeye devam edebilirsiniz.",
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
                // Settings & Account Management Tab
                item {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // --- 10 TL IN-APP BILLING SUPPORT (Sade ve göze batmayan tasarım) ---
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("support_billing_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSupporter) Icons.Default.Check else Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (isSupporter) GoogleGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isSupporter) "Geliştirici Destekçisi" else "Geliştiriciye Destek Ol",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isSupporter) "Katkınız için teşekkürler (10 ₺)" else "10 ₺ Google Play katkısı",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        (context as? Activity)?.let { onBuyDeveloperSupport(it) }
                                            ?: onSimulateSupport()
                                    },
                                    modifier = Modifier.testTag("buy_support_10tl_button")
                                ) {
                                    Text(
                                        text = if (isSupporter) "Teşekkürler" else "Destekle",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // --- HESAP VE GİRİŞ YÖNETİMİ ---
                        Text(
                            text = "Hesap & Google İşlemleri",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoogleBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Google Sign-Out
                        if (firebaseUser != null) {
                            SettingItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                iconColor = GoogleRed,
                                title = "Google ile Giriş Yapma İşlemini Sonlandır",
                                subtitle = "Aktif oturumu kapatır (${firebaseUser.email ?: "Google Hesabı"})",
                                onClick = { showSignOutDialog = true }
                            )
                        } else {
                            SettingItem(
                                icon = Icons.Default.SwitchAccount,
                                iconColor = GoogleBlue,
                                title = "Google ile Giriş Yap",
                                subtitle = "Podcast'lerinizi Google hesabınıza senkronize edin",
                                onClick = onOpenAuthDialog
                            )
                        }

                        // Delete Account and Cloud Data
                        SettingItem(
                            icon = Icons.Default.DeleteForever,
                            iconColor = MaterialTheme.colorScheme.error,
                            title = "Hesap Bilgilerini ve Verilerini Sil",
                            subtitle = "Google hesabına bağlı kaydedilen tüm podcast yayınlarını, favorileri ve kullanıcı verilerini kalıcı olarak siler",
                            onClick = { showDeleteAccountDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Oynatma & İndirme Ayarları",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoogleBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Default Speed
                        SettingItem(
                            icon = Icons.Default.Speed,
                            title = "Varsayılan Oynatma Hızı",
                            subtitle = "Tüm bölümler için standart hız ayarı",
                            onClick = { showSpeedDialog = true }
                        )

                        // Theme
                        val themeTitle = when (appThemeMode) {
                            1 -> "Açık Tema"
                            2 -> "Koyu Tema (AMOLED)"
                            else -> "Sistem Varsayılanı"
                        }
                        SettingItem(
                            icon = Icons.Default.Palette,
                            title = "Uygulama Teması",
                            subtitle = themeTitle,
                            onClick = { showThemeDialog = true }
                        )

                        // Auto Download Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Otomatik Bölüm İndirme", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Abone olduğunuz kanalların yeni bölümlerini otomatik cihaza kaydet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoDownload,
                                onCheckedChange = { onToggleAutoDownload() },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoogleBlue)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)

                        // Wi-Fi Only Download Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Yalnızca Wi-Fi ile İndir", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Mobil veri kullanımını engeller", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = wifiOnlyDownload,
                                onCheckedChange = { onToggleWifiOnly() },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoogleBlue)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Depolama & Cihaz Temizliği",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoogleBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Clear Downloads
                        SettingItem(
                            icon = Icons.Default.DeleteSweep,
                            title = "İndirilen Bölümleri Temizle",
                            subtitle = "Cihazınızdaki indirilen MP3 ses dosyalarını silerek depolama alanını boşaltır",
                            onClick = { showClearDownloadsDialog = true }
                        )

                        // Clear History
                        SettingItem(
                            icon = Icons.Default.History,
                            title = "Dinleme Geçmişini Sıfırla",
                            subtitle = "Tüm dinleme kayıtlarını ve ilerleme durumlarını temizler",
                            onClick = { showClearHistoryDialog = true }
                        )

                        // App Info
                        SettingItem(
                            icon = Icons.Outlined.Info,
                            title = "iRadyo Hakkında",
                            subtitle = "Sürüm 6.0 • Google Podcasts Mimarisi • Firebase & In-App Billing",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    // Purchase Result Alert
    purchaseMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onClearPurchaseMessage,
            title = { Text("iRadyo Destek", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onClearPurchaseMessage) {
                    Text("Tamam")
                }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        val themeOptions = listOf("Sistem Varsayılanı", "Açık Tema", "Koyu Tema (AMOLED)")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema Seçimi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themeOptions.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetThemeMode(index)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, fontSize = 15.sp, fontWeight = if (appThemeMode == index) FontWeight.Bold else FontWeight.Normal)
                            if (appThemeMode == index) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoogleBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Kapat") }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Google Oturumunu Sonlandır", fontWeight = FontWeight.Bold) },
            text = { Text("Google hesabınızdan çıkış yapılacaktır. Çevrimdışı içerikleriniz cihazda kalır, bulut senkronizasyonu duraklatılır.") },
            confirmButton = {
                Button(
                    onClick = {
                        onSignOutGoogle()
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Oturumu Kapat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Vazgeç") }
            }
        )
    }

    // Delete Account & Data Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hesap Bilgilerini ve Verilerini Sil", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "Bu işlemle Google hesabınıza bağlı tüm kaydedilen podcastler, bölümler, favoriler ve dinleme geçmişiniz Firebase bulutundan ve cihazınızdan kalıcı olarak silinecektir.\n\nBu işlem geri alınamaz. Devam etmek istiyor musunuz?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccountAndData()
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("Evet, Tüm Verileri Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Vazgeç") }
            }
        )
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Geçmişi Temizle", fontWeight = FontWeight.Bold) },
            text = { Text("Tüm dinleme geçmişiniz ve bölümlerdeki ilerlemeleriniz silinecektir. Emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Temizle", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Vazgeç") }
            }
        )
    }

    // Clear Downloads Confirmation Dialog
    if (showClearDownloadsDialog) {
        AlertDialog(
            onDismissRequest = { showClearDownloadsDialog = false },
            title = { Text("İndirilenleri Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Cihazınızda çevrimdışı saklanan tüm indirilmiş podcast MP3 dosyaları silinecek ve depolama alanı boşaltılacaktır.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearDownloads()
                        showClearDownloadsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDownloadsDialog = false }) { Text("Vazgeç") }
            }
        )
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
    iconColor: Color = GoogleBlue,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
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

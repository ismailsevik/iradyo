package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.EpisodeEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.PodcastEntity
import com.example.data.local.UserEntity
import com.example.data.model.Category
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastRepository(private val database: AppDatabase) {

    private val podcastDao = database.podcastDao()
    private val userDao = database.userDao()

    private val _currentUserId = MutableStateFlow("user_ismail")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
            loadInitialUser()
        }
    }

    private suspend fun loadInitialUser() {
        val user = userDao.getUserById(_currentUserId.value)
        if (user != null) {
            _currentUser.value = UserProfile(
                id = user.id,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                avatarColorHex = user.avatarColorHex,
                createdAt = user.createdAt,
                totalListeningMinutes = user.totalListeningMinutes
            )
        }
    }

    val categories = listOf(
        Category("tech", "Teknoloji", "computer", "#1A73E8", 18),
        Category("news", "Haber ve Gündem", "newspaper", "#EA4335", 24),
        Category("comedy", "Komedi & Mizah", "theater_comedy", "#FBBC04", 15),
        Category("business", "İş ve Girişim", "trending_up", "#34A853", 12),
        Category("science", "Bilim ve Keşif", "science", "#1A73E8", 16),
        Category("culture", "Kültür ve Sanat", "palette", "#9C27B0", 14),
        Category("history", "Tarih & Belgesel", "history_edu", "#E65100", 11),
        Category("radio", "Canlı iRadyo", "radio", "#00897B", 8)
    )

    fun getAllPodcasts(): Flow<List<Podcast>> =
        podcastDao.getAllPodcasts().map { list -> list.map { it.toDomain() } }

    fun getSubscribedPodcasts(): Flow<List<Podcast>> =
        podcastDao.getSubscribedPodcasts().map { list -> list.map { it.toDomain() } }

    fun getPodcastsByCategory(category: String): Flow<List<Podcast>> =
        podcastDao.getPodcastsByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getAllEpisodes(): Flow<List<Episode>> =
        podcastDao.getAllEpisodes().map { list -> list.map { it.toDomain() } }

    fun getEpisodesForPodcast(podcastId: String): Flow<List<Episode>> =
        podcastDao.getEpisodesForPodcast(podcastId).map { list -> list.map { it.toDomain() } }

    fun getQueuedEpisodes(): Flow<List<Episode>> =
        podcastDao.getQueuedEpisodes().map { list -> list.map { it.toDomain() } }

    fun getDownloadedEpisodes(): Flow<List<Episode>> =
        podcastDao.getDownloadedEpisodes().map { list -> list.map { it.toDomain() } }

    suspend fun getPodcastById(podcastId: String): Podcast? = withContext(Dispatchers.IO) {
        podcastDao.getPodcastById(podcastId)?.toDomain()
    }

    suspend fun getEpisodeById(episodeId: String): Episode? = withContext(Dispatchers.IO) {
        podcastDao.getEpisodeById(episodeId)?.toDomain()
    }

    suspend fun toggleSubscription(podcastId: String) = withContext(Dispatchers.IO) {
        val podcast = podcastDao.getPodcastById(podcastId) ?: return@withContext
        val newSub = !podcast.isSubscribed
        podcastDao.updateSubscription(podcastId, newSub)
    }

    suspend fun toggleFavoritePodcast(podcastId: String) = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value
        val podcast = podcastDao.getPodcastById(podcastId) ?: return@withContext
        val newFav = !podcast.isFavorited
        podcastDao.updateFavorite(podcastId, newFav)

        if (newFav) {
            userDao.insertFavorite(FavoriteEntity(userId = uid, podcastId = podcastId))
        } else {
            userDao.removeFavorite(uid, podcastId)
        }
    }

    suspend fun toggleEpisodeFavorite(episodeId: String) = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext
        val newFav = !episode.isFavorited
        podcastDao.updateEpisodeFavorite(episodeId, newFav)
    }

    suspend fun toggleQueue(episodeId: String) = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext
        val newQueue = !episode.isInQueue
        podcastDao.updateQueueStatus(episodeId, newQueue)
    }

    suspend fun toggleDownload(episodeId: String) = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext
        val newDown = !episode.isDownloaded
        podcastDao.updateDownloadStatus(episodeId, newDown)
    }

    suspend fun updatePlaybackPosition(episodeId: String, positionSeconds: Int, isPlayed: Boolean) =
        withContext(Dispatchers.IO) {
            podcastDao.updatePlaybackPosition(episodeId, positionSeconds, isPlayed)
            val uid = _currentUserId.value
            userDao.insertHistory(
                HistoryEntity(
                    userId = uid,
                    episodeId = episodeId,
                    listenedAt = System.currentTimeMillis(),
                    progressSeconds = positionSeconds,
                    completed = isPlayed
                )
            )
            // Add listening time to user stats
            if (positionSeconds > 60) {
                userDao.addListeningTime(uid, 1)
                loadInitialUser()
            }
        }

    // User Profile & Authentication
    fun getFavoritePodcastsForCurrentUser(): Flow<List<Podcast>> {
        return podcastDao.getAllPodcasts().map { list ->
            list.filter { it.isFavorited }.map { it.toDomain() }
        }
    }

    fun getListeningHistoryForCurrentUser(): Flow<List<Episode>> {
        return podcastDao.getAllEpisodes().map { list ->
            list.filter { it.playbackPositionSeconds > 0 || it.isPlayed }
        }.map { list -> list.map { it.toDomain() } }
    }

    suspend fun registerUser(username: String, email: String, fullName: String, passwordHash: String): Boolean =
        withContext(Dispatchers.IO) {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) return@withContext false

            val colors = listOf("#1A73E8", "#EA4335", "#FBBC04", "#34A853", "#9C27B0")
            val chosenColor = colors.random()
            val newUserId = "user_${System.currentTimeMillis()}"
            val entity = UserEntity(
                id = newUserId,
                username = username,
                email = email,
                fullName = fullName,
                avatarColorHex = chosenColor,
                passwordHash = passwordHash,
                createdAt = System.currentTimeMillis(),
                totalListeningMinutes = 0
            )
            userDao.insertUser(entity)
            _currentUserId.value = newUserId
            loadInitialUser()
            true
        }

    suspend fun loginUser(email: String, passwordHash: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email) ?: return@withContext false
        if (user.passwordHash.isNotEmpty() && user.passwordHash != passwordHash) {
            return@withContext false
        }
        _currentUserId.value = user.id
        loadInitialUser()
        true
    }

    suspend fun switchUser(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext
        _currentUserId.value = user.id
        _currentUser.value = UserProfile(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            avatarColorHex = user.avatarColorHex,
            createdAt = user.createdAt,
            totalListeningMinutes = user.totalListeningMinutes
        )
    }

    fun getAllUsers(): Flow<List<UserProfile>> = userDao.getAllUsers().map { list ->
        list.map {
            UserProfile(
                id = it.id,
                username = it.username,
                email = it.email,
                fullName = it.fullName,
                avatarColorHex = it.avatarColorHex,
                createdAt = it.createdAt,
                totalListeningMinutes = it.totalListeningMinutes
            )
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val existing = podcastDao.getAllPodcasts().first()
        if (existing.isNotEmpty()) return

        // Default User
        val defaultUser = UserEntity(
            id = "user_ismail",
            username = "ismailsevik",
            email = "ismailseviktr@gmail.com",
            fullName = "İsmail Şevik",
            avatarColorHex = "#1A73E8",
            passwordHash = "123456",
            createdAt = System.currentTimeMillis() - 86400000L * 45,
            totalListeningMinutes = 340
        )
        userDao.insertUser(defaultUser)

        val samplePodcasts = listOf(
            PodcastEntity(
                id = "pod_1",
                title = "Fularsız Entellik",
                author = "İmmanuel Tolstoyevski",
                description = "Halk için halka rağmen entellik. Felsefe, bilim, bilişsel yanılgılar ve popüler kültür üzerine derin ve esprili sohbetler.",
                imageUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
                category = "Bilim ve Keşif",
                totalEpisodes = 48,
                subscriberCount = 84000,
                isSubscribed = true,
                isFavorited = true,
                rating = 4.9
            ),
            PodcastEntity(
                id = "pod_2",
                title = "TeknoSeyir Haftalık Gündem",
                author = "Murat Gamsız & Levent Pekcan",
                description = "Bilişim dünyasında haftanın öne çıkan gelişmeleri, donanım, yazılım, yapay zeka ve internet dünyası değerlendirmeleri.",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                category = "Teknoloji",
                totalEpisodes = 620,
                subscriberCount = 145000,
                isSubscribed = true,
                isFavorited = true,
                rating = 4.8
            ),
            PodcastEntity(
                id = "pod_3",
                title = "Nasıl Olunur?",
                author = "Nilay Örnek",
                description = "Mesleklerini ustalıkla icra eden insanlarla 'nasıl olunur' sorusuna yanıt arayan derinlikli röportaj serisi.",
                imageUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80",
                category = "Kültür ve Sanat",
                totalEpisodes = 175,
                subscriberCount = 112000,
                isSubscribed = true,
                isFavorited = false,
                rating = 4.9
            ),
            PodcastEntity(
                id = "pod_4",
                title = "Açık Radyo: Günün ve Geleceğin Yankısı",
                author = "Ömer Madra & Açık Gazete Ekibi",
                description = "Kainatın tüm seslerine, renklerine ve titreşimlerine açık radyo yayını. Dünya haberleri, iklim ve kültür sanat.",
                imageUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=600&auto=format&fit=crop&q=80",
                category = "Haber ve Gündem",
                totalEpisodes = 340,
                subscriberCount = 67000,
                isSubscribed = false,
                isFavorited = false,
                rating = 4.7
            ),
            PodcastEntity(
                id = "pod_5",
                title = "Meksika Açmazı",
                author = "Mesut Süre, Fazlı Polat & Anlatanadam",
                description = "Çıkmaza giren durumlar, ahlaki ikilemler ve kahkaha dolu absürt tartışmalar.",
                imageUrl = "https://images.unsplash.com/photo-1589903102059-29179f977d07?w=600&auto=format&fit=crop&q=80",
                category = "Komedi & Mizah",
                totalEpisodes = 130,
                subscriberCount = 189000,
                isSubscribed = true,
                isFavorited = true,
                rating = 4.9
            ),
            PodcastEntity(
                id = "pod_6",
                title = "Girişimci Muhabbeti",
                author = "Sami Bugay & Barış Koçdur",
                description = "Start-up ekosistemi, teknoloji yatırımları, ürün geliştirme ve küresel ölçeklenme stratejileri.",
                imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600&auto=format&fit=crop&q=80",
                category = "İş ve Girişim",
                totalEpisodes = 88,
                subscriberCount = 42000,
                isSubscribed = false,
                isFavorited = false,
                rating = 4.6
            ),
            PodcastEntity(
                id = "pod_7",
                title = "Tarihin Arka Odası & Mitoloji",
                author = "Doç. Dr. Emrah Safa Gürkan",
                description = "Tarihi klişeleri yıkan, belgeler ve analitik bakış açısıyla geçmişin sırlarını aydınlatan keyifli anlatım.",
                imageUrl = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600&auto=format&fit=crop&q=80",
                category = "Tarih & Belgesel",
                totalEpisodes = 74,
                subscriberCount = 95000,
                isSubscribed = false,
                isFavorited = false,
                rating = 4.8
            ),
            PodcastEntity(
                id = "pod_8",
                title = "iRadyo Klasik & Caz Akışı",
                author = "iRadyo Müzik Odası",
                description = "Kesintisiz yüksek kaliteli canlı radyo akışı, klasik müzik seçkileri ve sakinleştirici akustik seanslar.",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                category = "Canlı iRadyo",
                totalEpisodes = 20,
                subscriberCount = 38000,
                isSubscribed = true,
                isFavorited = true,
                rating = 4.9
            )
        )
        podcastDao.insertPodcasts(samplePodcasts)

        // Seed Episodes with high reliability free streaming MP3 audio
        val audioUrls = listOf(
            "https://actions.google.com/sounds/v1/ambiences/coffee_shop.ogg",
            "https://actions.google.com/sounds/v1/science_fiction/ambient_space_hum.ogg",
            "https://actions.google.com/sounds/v1/weather/rain_heavy.ogg",
            "https://actions.google.com/sounds/v1/water/waves_crashing_on_rocks.ogg",
            "https://actions.google.com/sounds/v1/household/clock_ticking_fast.ogg"
        )

        val sampleEpisodes = listOf(
            EpisodeEntity(
                id = "ep_1_1",
                podcastId = "pod_1",
                podcastTitle = "Fularsız Entellik",
                podcastImageUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
                title = "Bölüm 48: Yapay Zeka Gerçekten Düşünebilir mi?",
                description = "Büyük dil modelleri, Turing testi, Çin Odası deneyi ve bilincin mekanik kökenleri üzerine felsefi bir yolculuk.",
                audioUrl = audioUrls[0],
                durationSeconds = 2460, // 41 min
                releaseDate = "Dün",
                isPlayed = false,
                playbackPositionSeconds = 620,
                isDownloaded = true,
                isFavorited = true,
                isInQueue = true
            ),
            EpisodeEntity(
                id = "ep_1_2",
                podcastId = "pod_1",
                podcastTitle = "Fularsız Entellik",
                podcastImageUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
                title = "Bölüm 47: Sosyal Medyada Doğrulama Yanılgısı (Confirmation Bias)",
                description = "Neden sadece duymak istediklerimizi duyuyoruz? Yankı fanusları ve algoritma tuzakları.",
                audioUrl = audioUrls[1],
                durationSeconds = 1980,
                releaseDate = "12 Eki",
                isPlayed = true,
                playbackPositionSeconds = 1980,
                isDownloaded = false,
                isFavorited = false,
                isInQueue = false
            ),
            EpisodeEntity(
                id = "ep_2_1",
                podcastId = "pod_2",
                podcastTitle = "TeknoSeyir Haftalık Gündem",
                podcastImageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                title = "Haftalık Gündem Değerlendirmesi 2026/42",
                description = "Yonga krizi son durumu, yeni nesil kuantum işlemciler, açık kaynak işletim sistemleri ve haftanın siber güvenlik uyarıları.",
                audioUrl = audioUrls[1],
                durationSeconds = 3840, // 64 min
                releaseDate = "Bugün",
                isPlayed = false,
                playbackPositionSeconds = 120,
                isDownloaded = true,
                isFavorited = true,
                isInQueue = true
            ),
            EpisodeEntity(
                id = "ep_2_2",
                podcastId = "pod_2",
                podcastTitle = "TeknoSeyir Haftalık Gündem",
                podcastImageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                title = "Tozlu Raflar: MP3 Formatının Doğuşu ve Devrimi",
                description = "Fraunhofer Enstitüsü, Napster fırtınası ve dijital müziğin tüm dünyayı nasıl baştan aşağı değiştirdiği.",
                audioUrl = audioUrls[0],
                durationSeconds = 2700,
                releaseDate = "3 gün önce",
                isPlayed = false,
                playbackPositionSeconds = 0,
                isDownloaded = false,
                isFavorited = false,
                isInQueue = false
            ),
            EpisodeEntity(
                id = "ep_3_1",
                podcastId = "pod_3",
                podcastTitle = "Nasıl Olunur?",
                podcastImageUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80",
                title = "Bölüm 175: Nuri Bilge Ceylan ile Sinemada Sessizliğin Gücü",
                description = "Karakterlerin iç dünyası, uzun planlar, taşra psikolojisi ve bir filmi baştan sona kurgulamak.",
                audioUrl = audioUrls[2],
                durationSeconds = 3120,
                releaseDate = "5 gün önce",
                isPlayed = false,
                playbackPositionSeconds = 0,
                isDownloaded = false,
                isFavorited = true,
                isInQueue = true
            ),
            EpisodeEntity(
                id = "ep_5_1",
                podcastId = "pod_5",
                podcastTitle = "Meksika Açmazı",
                podcastImageUrl = "https://images.unsplash.com/photo-1589903102059-29179f977d07?w=600&auto=format&fit=crop&q=80",
                title = "Bölüm 130: 'Düğünde Takılan Sahte Altın Açmazı'",
                description = "En yakın arkadaşının düğününde başına gelebilecek en büyük felaketler ve Fazlı'nın çözülemeyen taktikleri.",
                audioUrl = audioUrls[3],
                durationSeconds = 2520,
                releaseDate = "6 gün önce",
                isPlayed = false,
                playbackPositionSeconds = 0,
                isDownloaded = false,
                isFavorited = true,
                isInQueue = false
            ),
            EpisodeEntity(
                id = "ep_8_1",
                podcastId = "pod_8",
                podcastTitle = "iRadyo Klasik & Caz Akışı",
                podcastImageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                title = "Canlı Yayın: Gece Cazı ve Yağmur Akustiği",
                description = "Miles Davis, Bill Evans ve John Coltrane eşliğinde 7/24 kesintisiz iRadyo stüdyo canlı akışı.",
                audioUrl = audioUrls[2],
                durationSeconds = 7200,
                releaseDate = "Canlı",
                isPlayed = false,
                playbackPositionSeconds = 450,
                isDownloaded = true,
                isFavorited = true,
                isInQueue = false
            )
        )
        podcastDao.insertEpisodes(sampleEpisodes)
    }

    private fun PodcastEntity.toDomain() = Podcast(
        id = id,
        title = title,
        author = author,
        description = description,
        imageUrl = imageUrl,
        category = category,
        totalEpisodes = totalEpisodes,
        subscriberCount = subscriberCount,
        isSubscribed = isSubscribed,
        isFavorited = isFavorited,
        rating = rating
    )

    private fun EpisodeEntity.toDomain() = Episode(
        id = id,
        podcastId = podcastId,
        podcastTitle = podcastTitle,
        podcastImageUrl = podcastImageUrl,
        title = title,
        description = description,
        audioUrl = audioUrl,
        durationSeconds = durationSeconds,
        releaseDate = releaseDate,
        isPlayed = isPlayed,
        playbackPositionSeconds = playbackPositionSeconds,
        isDownloaded = isDownloaded,
        isFavorited = isFavorited,
        isInQueue = isInQueue
    )
}

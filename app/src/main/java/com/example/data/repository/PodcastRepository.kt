package com.example.data.repository

import android.content.Context
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
import com.example.data.remote.PodcastRssClient
import com.example.util.PodcastDownloadHelper
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

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            cleanupStaleDefaultData()
            loadActiveUser()
        }
    }

    private suspend fun cleanupStaleDefaultData() {
        // Clean predetermined sample podcasts, dummy users, and fake records
        try {
            userDao.deleteUserById("user_ismail")
            userDao.clearHistory("user_ismail")
            listOf("pod_1", "pod_2", "pod_3", "pod_4", "pod_5", "pod_6", "pod_7", "pod_8").forEach { id ->
                podcastDao.deletePodcastById(id)
                podcastDao.deleteEpisodesByPodcastId(id)
                userDao.removeFavorite("user_ismail", id)
            }
        } catch (_: Exception) {}
    }

    private suspend fun loadActiveUser() {
        val uid = _currentUserId.value
        if (uid.isBlank()) {
            _currentUser.value = null
            return
        }
        val user = userDao.getUserById(uid)
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
        } else {
            _currentUser.value = null
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

    fun getFavoriteEpisodes(): Flow<List<Episode>> =
        podcastDao.getFavoriteEpisodes().map { list -> list.map { it.toDomain() } }

    fun searchLocalEpisodes(query: String): Flow<List<Episode>> =
        podcastDao.searchEpisodes(query).map { list -> list.map { it.toDomain() } }

    suspend fun getPodcastById(podcastId: String): Podcast? = withContext(Dispatchers.IO) {
        podcastDao.getPodcastById(podcastId)?.toDomain()
    }

    suspend fun getEpisodeById(episodeId: String): Episode? = withContext(Dispatchers.IO) {
        podcastDao.getEpisodeById(episodeId)?.toDomain()
    }

    suspend fun searchOnlinePodcasts(query: String): List<Podcast> = withContext(Dispatchers.IO) {
        PodcastRssClient.searchPodcasts(query)
    }

    suspend fun fetchOnlineEpisodesForPodcast(podcast: Podcast, feedUrl: String): List<Episode> = withContext(Dispatchers.IO) {
        val episodes = PodcastRssClient.fetchEpisodesForFeed(podcast, feedUrl)
        if (episodes.isNotEmpty()) {
            podcastDao.insertEpisodes(episodes.map { it.toEntity() })
        }
        episodes
    }

    suspend fun savePodcast(podcast: Podcast) = withContext(Dispatchers.IO) {
        podcastDao.insertPodcasts(listOf(podcast.toEntity()))
    }

    suspend fun toggleSubscription(podcastId: String): Boolean = withContext(Dispatchers.IO) {
        val podcast = podcastDao.getPodcastById(podcastId) ?: return@withContext false
        val newSub = !podcast.isSubscribed
        podcastDao.updateSubscription(podcastId, newSub)
        newSub
    }

    suspend fun toggleFavoritePodcast(podcastId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value
        val podcast = podcastDao.getPodcastById(podcastId) ?: return@withContext false
        val newFav = !podcast.isFavorited
        podcastDao.updateFavorite(podcastId, newFav)

        if (uid.isNotBlank()) {
            if (newFav) {
                userDao.insertFavorite(FavoriteEntity(userId = uid, podcastId = podcastId))
            } else {
                userDao.removeFavorite(uid, podcastId)
            }
        }
        newFav
    }

    suspend fun toggleEpisodeFavorite(episodeId: String): Episode? = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext null
        val newFav = !episode.isFavorited
        podcastDao.updateEpisodeFavorite(episodeId, newFav)
        episode.copy(isFavorited = newFav).toDomain()
    }

    suspend fun toggleQueue(episodeId: String) = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext
        val newQueue = !episode.isInQueue
        podcastDao.updateQueueStatus(episodeId, newQueue)
    }

    suspend fun toggleRealDownload(context: Context, episode: Episode): Boolean = withContext(Dispatchers.IO) {
        val existing = podcastDao.getEpisodeById(episode.id)
        if (existing == null) {
            podcastDao.insertEpisodes(listOf(episode.toEntity()))
        }
        val isDownloaded = existing?.isDownloaded == true || PodcastDownloadHelper.isDownloadedLocally(context, episode.id)

        if (isDownloaded) {
            PodcastDownloadHelper.deleteDownloadedEpisode(context, episode.id)
            podcastDao.updateDownloadStatus(episode.id, false)
            return@withContext false
        } else {
            val success = PodcastDownloadHelper.downloadEpisode(context, episode)
            if (success) {
                podcastDao.updateDownloadStatus(episode.id, true)
            }
            return@withContext success
        }
    }

    suspend fun toggleRealDownload(context: Context, episodeId: String): Boolean = withContext(Dispatchers.IO) {
        val episode = podcastDao.getEpisodeById(episodeId) ?: return@withContext false
        toggleRealDownload(context, episode.toDomain())
    }

    suspend fun updateEpisodeDuration(episodeId: String, duration: Int) = withContext(Dispatchers.IO) {
        if (duration > 0) {
            podcastDao.updateEpisodeDuration(episodeId, duration)
        }
    }

    suspend fun updatePlaybackPosition(episodeId: String, positionSeconds: Int, isPlayed: Boolean) =
        withContext(Dispatchers.IO) {
            podcastDao.updatePlaybackPosition(episodeId, positionSeconds, isPlayed)
            val uid = _currentUserId.value
            if (uid.isNotBlank()) {
                userDao.insertHistory(
                    HistoryEntity(
                        userId = uid,
                        episodeId = episodeId,
                        listenedAt = System.currentTimeMillis(),
                        progressSeconds = positionSeconds,
                        completed = isPlayed
                    )
                )
                if (positionSeconds > 60) {
                    userDao.addListeningTime(uid, 1)
                    loadActiveUser()
                }
            }
        }

    suspend fun clearListeningHistory() = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value
        if (uid.isNotBlank()) {
            userDao.clearHistory(uid)
        }
        val episodes = podcastDao.getAllEpisodes().first()
        episodes.forEach { ep ->
            if (ep.playbackPositionSeconds > 0 || ep.isPlayed) {
                podcastDao.updatePlaybackPosition(ep.id, 0, false)
            }
        }
    }

    suspend fun clearDownloads(context: Context) = withContext(Dispatchers.IO) {
        PodcastDownloadHelper.clearAllLocalDownloads(context)
        val episodes = podcastDao.getDownloadedEpisodes().first()
        episodes.forEach { ep ->
            podcastDao.updateDownloadStatus(ep.id, false)
        }
    }

    suspend fun clearAllUserAndDefaultData(context: Context) = withContext(Dispatchers.IO) {
        // Clear all user tables
        userDao.deleteAllUsers()
        userDao.clearAllFavorites()
        userDao.clearAllHistory()

        // Reset all podcast user statuses
        podcastDao.resetPodcastsUserData()
        podcastDao.resetEpisodesUserData()

        // Delete all local audio files
        PodcastDownloadHelper.clearAllLocalDownloads(context)

        _currentUserId.value = ""
        _currentUser.value = null
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
            if (existing != null) {
                _currentUserId.value = existing.id
                loadActiveUser()
                return@withContext true
            }

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
            loadActiveUser()
            true
        }

    suspend fun setFirebaseUser(uid: String, email: String, displayName: String) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserById(uid)
        if (existing == null) {
            val entity = UserEntity(
                id = uid,
                username = email.substringBefore("@"),
                email = email,
                fullName = displayName.ifBlank { "Google Kullanıcısı" },
                avatarColorHex = "#1A73E8",
                passwordHash = "",
                createdAt = System.currentTimeMillis(),
                totalListeningMinutes = 0
            )
            userDao.insertUser(entity)
        }
        _currentUserId.value = uid
        loadActiveUser()
    }

    suspend fun clearCurrentUser() = withContext(Dispatchers.IO) {
        _currentUserId.value = ""
        _currentUser.value = null
    }

    suspend fun loginUser(email: String, passwordHash: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email) ?: return@withContext false
        if (user.passwordHash.isNotEmpty() && user.passwordHash != passwordHash) {
            return@withContext false
        }
        _currentUserId.value = user.id
        loadActiveUser()
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

    fun Podcast.toEntity() = PodcastEntity(
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

    fun Episode.toEntity() = EpisodeEntity(
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

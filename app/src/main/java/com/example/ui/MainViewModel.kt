package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.BillingManager
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Episode
import com.example.data.model.PlayerState
import com.example.data.model.Podcast
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseSyncRepository
import com.example.data.repository.PodcastRepository
import com.example.player.AudioPlayerManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("iradyo_settings", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application)
    val repository = PodcastRepository(database)
    val firebaseSync = FirebaseSyncRepository(application)

    val billingManager = BillingManager(application) {
        // Supporter unlocked!
    }

    val isSupporter: StateFlow<Boolean> = billingManager.isSupporter
    val purchaseMessage: StateFlow<String?> = billingManager.purchaseMessage

    val firebaseUser: StateFlow<FirebaseUser?> = firebaseSync.firebaseUser
    val isSyncing: StateFlow<Boolean> = firebaseSync.isSyncing
    val lastSyncTimestamp: StateFlow<Long?> = firebaseSync.lastSyncTimestamp

    val playerManager = AudioPlayerManager(application) { episodeId, positionSec, isCompleted ->
        viewModelScope.launch {
            repository.updatePlaybackPosition(episodeId, positionSec, isCompleted)
            repository.getEpisodeById(episodeId)?.let { ep ->
                firebaseSync.syncHistoryToCloud(ep.copy(playbackPositionSeconds = positionSec, isPlayed = isCompleted))
            }
        }
    }

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    // Navigation and UI states
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Explore, 2: Activity, 3: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activitySubTab = MutableStateFlow(0) // 0: Queue, 1: Downloads, 2: Favorites, 3: History, 4: Subscriptions
    val activitySubTab: StateFlow<Int> = _activitySubTab.asStateFlow()

    private val _homeFilterChip = MutableStateFlow(0) // 0: All, 1: Downloaded, 2: In progress
    val homeFilterChip: StateFlow<Int> = _homeFilterChip.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedPodcastDetail = MutableStateFlow<Podcast?>(null)
    val selectedPodcastDetail: StateFlow<Podcast?> = _selectedPodcastDetail.asStateFlow()

    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isAuthDialogVisible = MutableStateFlow(false)
    val isAuthDialogVisible: StateFlow<Boolean> = _isAuthDialogVisible.asStateFlow()

    // Real Online Search States
    private val _onlineSearchResults = MutableStateFlow<List<Podcast>>(emptyList())
    val onlineSearchResults: StateFlow<List<Podcast>> = _onlineSearchResults.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    private var searchJob: Job? = null

    // App Settings States
    private val _appThemeMode = MutableStateFlow(prefs.getInt("theme_mode", 0)) // 0: System, 1: Light, 2: Dark
    val appThemeMode: StateFlow<Int> = _appThemeMode.asStateFlow()

    private val _autoDownload = MutableStateFlow(prefs.getBoolean("auto_download", false))
    val autoDownload: StateFlow<Boolean> = _autoDownload.asStateFlow()

    private val _wifiOnlyDownload = MutableStateFlow(prefs.getBoolean("wifi_only", true))
    val wifiOnlyDownload: StateFlow<Boolean> = _wifiOnlyDownload.asStateFlow()

    // Data streams
    val categories = repository.categories

    val allPodcasts: StateFlow<List<Podcast>> = repository.getAllPodcasts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscribedPodcasts: StateFlow<List<Podcast>> = repository.getSubscribedPodcasts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEpisodes: StateFlow<List<Episode>> = repository.getAllEpisodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queuedEpisodes: StateFlow<List<Episode>> = repository.getQueuedEpisodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedEpisodes: StateFlow<List<Episode>> = repository.getDownloadedEpisodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePodcasts: StateFlow<List<Podcast>> = repository.getFavoritePodcastsForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteEpisodes: StateFlow<List<Episode>> = repository.getFavoriteEpisodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listeningHistory: StateFlow<List<Episode>> = repository.getListeningHistoryForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val allUsers: StateFlow<List<UserProfile>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            queuedEpisodes.collect { queue ->
                playerManager.updateQueue(queue)
            }
        }

        // When Firebase user state changes, sync to repository
        viewModelScope.launch {
            firebaseUser.collect { fbUser ->
                if (fbUser != null) {
                    val email = fbUser.email ?: ""
                    val name = fbUser.displayName ?: "Google Kullanıcısı"
                    repository.setFirebaseUser(fbUser.uid, email, name)
                    restoreCloudData()
                }
            }
        }
    }

    private fun restoreCloudData() {
        viewModelScope.launch {
            val cloudFavs = firebaseSync.fetchCloudFavorites()
            cloudFavs.forEach { podId ->
                repository.toggleFavoritePodcast(podId)
            }
            val cloudSubs = firebaseSync.fetchCloudSubscriptions()
            cloudSubs.forEach { podId ->
                repository.toggleSubscription(podId)
            }
            val cloudSavedEpisodes = firebaseSync.fetchCloudSavedEpisodes()
            if (cloudSavedEpisodes.isNotEmpty()) {
                cloudSavedEpisodes.forEach { ep ->
                    repository.toggleEpisodeFavorite(ep.id)
                }
            }
        }
    }

    // Filtered explore podcasts combining local and online live search
    val exploreFilteredPodcasts: StateFlow<List<Podcast>> = combine(
        allPodcasts,
        onlineSearchResults,
        searchQuery,
        selectedCategory
    ) { localPodcasts, onlineList, query, category ->
        if (query.isNotBlank() && onlineList.isNotEmpty()) {
            val localMap = localPodcasts.associateBy { it.id }
            val mergedOnline = onlineList.map { onlinePod ->
                localMap[onlinePod.id]?.let { localPod ->
                    onlinePod.copy(isSubscribed = localPod.isSubscribed, isFavorited = localPod.isFavorited)
                } ?: onlinePod
            }
            val combined = (mergedOnline + localPodcasts).distinctBy { it.id }
            if (category != null) {
                combined.filter { it.category.contains(category.name, ignoreCase = true) }
            } else combined
        } else {
            localPodcasts.filter { podcast ->
                val matchesCategory = category == null || podcast.category.contains(category.name, ignoreCase = true)
                val matchesQuery = query.isBlank() ||
                        podcast.title.contains(query, ignoreCase = true) ||
                        podcast.author.contains(query, ignoreCase = true) ||
                        podcast.description.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered home episodes
    val homeFilteredEpisodes: StateFlow<List<Episode>> = combine(
        allEpisodes,
        homeFilterChip
    ) { episodes, chip ->
        when (chip) {
            1 -> episodes.filter { it.isDownloaded }
            2 -> episodes.filter { it.playbackPositionSeconds > 0 && !it.isPlayed }
            else -> episodes
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectActivitySubTab(subTab: Int) {
        _activitySubTab.value = subTab
    }

    fun selectHomeFilter(chip: Int) {
        _homeFilterChip.value = chip
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                delay(400L) // debounce
                _isSearchingOnline.value = true
                val results = repository.searchOnlinePodcasts(query)
                _onlineSearchResults.value = results
                _isSearchingOnline.value = false
            }
        } else {
            _onlineSearchResults.value = emptyList()
            _isSearchingOnline.value = false
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun openPodcastDetail(podcast: Podcast) {
        _selectedPodcastDetail.value = podcast
        // Fetch online episodes if feedUrl is available
        val feedUrl = extractFeedUrl(podcast.description)
        if (feedUrl.isNotBlank()) {
            viewModelScope.launch {
                repository.fetchOnlineEpisodesForPodcast(podcast, feedUrl)
            }
        }
    }

    private fun extractFeedUrl(desc: String): String {
        return if (desc.contains("RSS:") || desc.contains("RSS Kaynağı:")) {
            val part = desc.substringAfter("RSS").substringAfter(":")
            part.trim().split(" ").firstOrNull() ?: ""
        } else if (desc.startsWith("http")) {
            desc.trim()
        } else ""
    }

    fun closePodcastDetail() {
        _selectedPodcastDetail.value = null
    }

    fun showFullPlayer() {
        _isFullPlayerVisible.value = true
    }

    fun hideFullPlayer() {
        _isFullPlayerVisible.value = false
    }

    fun showAuthDialog() {
        _isAuthDialogVisible.value = true
    }

    fun hideAuthDialog() {
        _isAuthDialogVisible.value = false
    }

    // Player controls
    fun playEpisode(episode: Episode) {
        playerManager.playEpisode(episode)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun rewind10() {
        playerManager.rewind10Seconds()
    }

    fun forward30() {
        playerManager.forward30Seconds()
    }

    fun seekTo(seconds: Int) {
        playerManager.seekToSeconds(seconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun toggleTrimSilence() {
        playerManager.toggleTrimSilence()
    }

    fun setSleepTimer(minutes: Int?) {
        playerManager.setSleepTimer(minutes)
    }

    // Settings Functions
    fun setThemeMode(mode: Int) {
        _appThemeMode.value = mode
        prefs.edit().putInt("theme_mode", mode).apply()
    }

    fun toggleAutoDownload() {
        val newVal = !_autoDownload.value
        _autoDownload.value = newVal
        prefs.edit().putBoolean("auto_download", newVal).apply()
    }

    fun toggleWifiOnlyDownload() {
        val newVal = !_wifiOnlyDownload.value
        _wifiOnlyDownload.value = newVal
        prefs.edit().putBoolean("wifi_only", newVal).apply()
    }

    fun clearListeningHistory() {
        viewModelScope.launch {
            repository.clearListeningHistory()
        }
    }

    fun clearDownloadedEpisodes() {
        viewModelScope.launch {
            repository.clearDownloads(getApplication())
        }
    }

    // In-App Billing: 10 TL Destek
    fun buyDeveloperSupport(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun simulateDeveloperSupport() {
        billingManager.simulateSuccessfulPurchase()
    }

    fun clearPurchaseMessage() {
        billingManager.clearMessage()
    }

    // User interactions + Firebase cloud synchronization
    fun toggleSubscription(podcastId: String) {
        viewModelScope.launch {
            val podcast = repository.getPodcastById(podcastId)
                ?: _onlineSearchResults.value.find { it.id == podcastId }

            if (podcast != null) {
                repository.savePodcast(podcast)
                val newSub = repository.toggleSubscription(podcastId)
                firebaseSync.syncSubscriptionToCloud(podcast, newSub)

                _onlineSearchResults.value = _onlineSearchResults.value.map {
                    if (it.id == podcastId) it.copy(isSubscribed = newSub) else it
                }

                _selectedPodcastDetail.value?.let { current ->
                    if (current.id == podcastId) {
                        _selectedPodcastDetail.value = current.copy(isSubscribed = newSub)
                    }
                }
            }
        }
    }

    fun toggleFavoritePodcast(podcastId: String) {
        viewModelScope.launch {
            val podcast = repository.getPodcastById(podcastId)
                ?: _onlineSearchResults.value.find { it.id == podcastId }

            if (podcast != null) {
                repository.savePodcast(podcast)
                val newFav = repository.toggleFavoritePodcast(podcastId)
                firebaseSync.syncFavoriteToCloud(podcast, newFav)

                _selectedPodcastDetail.value?.let { current ->
                    if (current.id == podcastId) {
                        _selectedPodcastDetail.value = current.copy(isFavorited = newFav)
                    }
                }
            }
        }
    }

    fun toggleFavoriteEpisode(episodeId: String) {
        viewModelScope.launch {
            val updated = repository.toggleEpisodeFavorite(episodeId)
            if (updated != null) {
                // Save/Sync to Google Account in Firestore
                firebaseSync.syncSavedEpisodeToCloud(updated, updated.isFavorited)
            }
        }
    }

    fun toggleQueue(episodeId: String) {
        viewModelScope.launch {
            repository.toggleQueue(episodeId)
        }
    }

    fun toggleDownload(episodeId: String) {
        viewModelScope.launch {
            repository.toggleRealDownload(getApplication(), episodeId)
        }
    }

    fun registerUser(username: String, email: String, fullName: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.registerUser(username, email, fullName, password)
            if (success) {
                hideAuthDialog()
            }
            onResult(success)
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.loginUser(email, password)
            if (success) {
                hideAuthDialog()
            }
            onResult(success)
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            hideAuthDialog()
        }
    }

    fun signInWithGoogleToken(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseSync.signInWithGoogleCredential(idToken)
            if (success) {
                firebaseSync.firebaseUser.value?.let { fbUser ->
                    val email = fbUser.email ?: "google@iradyo.app"
                    val name = fbUser.displayName ?: "Google Kullanıcısı"
                    repository.setFirebaseUser(fbUser.uid, email, name)
                    restoreCloudData()
                }
                hideAuthDialog()
            }
            onResult(success)
        }
    }

    fun signOutFirebase() {
        viewModelScope.launch {
            firebaseSync.signOut()
            repository.clearCurrentUser()
        }
    }

    // Delete Account, Cloud Data, and Local User Data
    fun deleteAccountAndAllData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cloudSuccess = firebaseSync.deleteAllUserDataFromCloud()
            repository.clearAllUserAndDefaultData(getApplication())
            onComplete(cloudSuccess)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        billingManager.destroy()
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Episode
import com.example.data.model.PlayerState
import com.example.data.model.Podcast
import com.example.data.model.UserProfile
import com.example.data.repository.PodcastRepository
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = PodcastRepository(database)

    val playerManager = AudioPlayerManager(application) { episodeId, positionSec, isCompleted ->
        viewModelScope.launch {
            repository.updatePlaybackPosition(episodeId, positionSec, isCompleted)
        }
    }

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    // Navigation and UI states
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Explore, 2: Activity, 3: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activitySubTab = MutableStateFlow(0) // 0: Queue, 1: Downloads, 2: History, 3: Subscriptions
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
    }

    // Filtered explore podcasts based on search and category
    val exploreFilteredPodcasts: StateFlow<List<Podcast>> = combine(
        allPodcasts,
        searchQuery,
        selectedCategory
    ) { podcasts, query, category ->
        podcasts.filter { podcast ->
            val matchesCategory = category == null || podcast.category.contains(category.name, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    podcast.title.contains(query, ignoreCase = true) ||
                    podcast.author.contains(query, ignoreCase = true) ||
                    podcast.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
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
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun openPodcastDetail(podcast: Podcast) {
        _selectedPodcastDetail.value = podcast
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

    // User interactions
    fun toggleSubscription(podcastId: String) {
        viewModelScope.launch {
            repository.toggleSubscription(podcastId)
            // Refresh podcast detail if currently viewed
            _selectedPodcastDetail.value?.let { current ->
                if (current.id == podcastId) {
                    _selectedPodcastDetail.value = current.copy(isSubscribed = !current.isSubscribed)
                }
            }
        }
    }

    fun toggleFavoritePodcast(podcastId: String) {
        viewModelScope.launch {
            repository.toggleFavoritePodcast(podcastId)
            _selectedPodcastDetail.value?.let { current ->
                if (current.id == podcastId) {
                    _selectedPodcastDetail.value = current.copy(isFavorited = !current.isFavorited)
                }
            }
        }
    }

    fun toggleFavoriteEpisode(episodeId: String) {
        viewModelScope.launch {
            repository.toggleEpisodeFavorite(episodeId)
        }
    }

    fun toggleQueue(episodeId: String) {
        viewModelScope.launch {
            repository.toggleQueue(episodeId)
        }
    }

    fun toggleDownload(episodeId: String) {
        viewModelScope.launch {
            repository.toggleDownload(episodeId)
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

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

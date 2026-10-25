package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AuthDialog
import com.example.ui.components.FullPlayerBottomSheet
import com.example.ui.components.GoogleSearchHeader
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PodcastDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activitySubTab by viewModel.activitySubTab.collectAsStateWithLifecycle()
    val homeFilterChip by viewModel.homeFilterChip.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPodcastDetail by viewModel.selectedPodcastDetail.collectAsStateWithLifecycle()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsStateWithLifecycle()
    val isAuthDialogVisible by viewModel.isAuthDialogVisible.collectAsStateWithLifecycle()

    val subscribedPodcasts by viewModel.subscribedPodcasts.collectAsStateWithLifecycle()
    val homeEpisodes by viewModel.homeFilteredEpisodes.collectAsStateWithLifecycle()
    val explorePodcasts by viewModel.exploreFilteredPodcasts.collectAsStateWithLifecycle()
    val allEpisodes by viewModel.allEpisodes.collectAsStateWithLifecycle()
    val queuedEpisodes by viewModel.queuedEpisodes.collectAsStateWithLifecycle()
    val downloadedEpisodes by viewModel.downloadedEpisodes.collectAsStateWithLifecycle()
    val favoritePodcasts by viewModel.favoritePodcasts.collectAsStateWithLifecycle()
    val listeningHistory by viewModel.listeningHistory.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    // Handle back button for sub-screens
    BackHandler(enabled = selectedPodcastDetail != null || selectedTab != 0) {
        if (selectedPodcastDetail != null) {
            viewModel.closePodcastDetail()
        } else if (selectedTab != 0) {
            viewModel.selectTab(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (selectedPodcastDetail == null) {
                    GoogleSearchHeader(
                        currentUser = currentUser,
                        searchQuery = searchQuery,
                        onSearchQueryChange = {
                            viewModel.setSearchQuery(it)
                            if (selectedTab != 1) {
                                viewModel.selectTab(1)
                            }
                        },
                        isSearchActive = selectedTab == 1,
                        onSearchBarClicked = {
                            viewModel.selectTab(1)
                        },
                        onProfileClicked = {
                            viewModel.showAuthDialog()
                        },
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Persistent Mini Player above Bottom Nav
                    if (playerState.currentEpisode != null && !isFullPlayerVisible) {
                        MiniPlayer(
                            playerState = playerState,
                            onExpandClicked = { viewModel.showFullPlayer() },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onRewind10 = { viewModel.rewind10() },
                            onForward30 = { viewModel.forward30() }
                        )
                    }

                    // Google Podcasts Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_navigation_bar")
                    ) {
                        val navItems = listOf(
                            Triple("Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home),
                            Triple("Keşfet", Icons.Filled.Explore, Icons.Outlined.Explore),
                            Triple("Etkinlik", Icons.Filled.Subscriptions, Icons.Outlined.Subscriptions),
                            Triple("Profil", Icons.Filled.Person, Icons.Outlined.Person)
                        )

                        navItems.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                            val isSelected = selectedTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (selectedPodcastDetail != null) {
                                        viewModel.closePodcastDetail()
                                    }
                                    viewModel.selectTab(index)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                                        contentDescription = label,
                                        tint = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_item_$index")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If viewing a Podcast Detail
                selectedPodcastDetail?.let { podcast ->
                    PodcastDetailScreen(
                        podcast = podcast,
                        episodes = allEpisodes,
                        currentPlayingEpisodeId = playerState.currentEpisode?.id,
                        isPlaying = playerState.isPlaying,
                        onBack = { viewModel.closePodcastDetail() },
                        onToggleSubscribe = { viewModel.toggleSubscription(podcast.id) },
                        onToggleFavorite = { viewModel.toggleFavoritePodcast(podcast.id) },
                        onPlayEpisode = { viewModel.playEpisode(it) },
                        onToggleQueue = { viewModel.toggleQueue(it.id) },
                        onToggleDownload = { viewModel.toggleDownload(it.id) },
                        onToggleFavoriteEpisode = { viewModel.toggleFavoriteEpisode(it.id) }
                    )
                } ?: run {
                    when (selectedTab) {
                        0 -> {
                            HomeScreen(
                                subscribedPodcasts = subscribedPodcasts,
                                episodes = homeEpisodes,
                                currentPlayingEpisodeId = playerState.currentEpisode?.id,
                                isPlaying = playerState.isPlaying,
                                selectedFilterChip = homeFilterChip,
                                onSelectFilterChip = { viewModel.selectHomeFilter(it) },
                                onPodcastClicked = { viewModel.openPodcastDetail(it) },
                                onNavigateToExplore = { viewModel.selectTab(1) },
                                onPlayEpisode = { viewModel.playEpisode(it) },
                                onToggleQueue = { viewModel.toggleQueue(it.id) },
                                onToggleDownload = { viewModel.toggleDownload(it.id) },
                                onToggleFavorite = { viewModel.toggleFavoriteEpisode(it.id) }
                            )
                        }
                        1 -> {
                            ExploreScreen(
                                categories = viewModel.categories,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                podcasts = explorePodcasts,
                                onPodcastClicked = { viewModel.openPodcastDetail(it) },
                                onToggleSubscribe = { viewModel.toggleSubscription(it) }
                            )
                        }
                        2 -> {
                            ActivityScreen(
                                selectedSubTab = activitySubTab,
                                onSelectSubTab = { viewModel.selectActivitySubTab(it) },
                                queuedEpisodes = queuedEpisodes,
                                downloadedEpisodes = downloadedEpisodes,
                                historyEpisodes = listeningHistory,
                                subscribedPodcasts = subscribedPodcasts,
                                currentPlayingEpisodeId = playerState.currentEpisode?.id,
                                isPlaying = playerState.isPlaying,
                                onPlayEpisode = { viewModel.playEpisode(it) },
                                onToggleQueue = { viewModel.toggleQueue(it.id) },
                                onToggleDownload = { viewModel.toggleDownload(it.id) },
                                onToggleFavorite = { viewModel.toggleFavoriteEpisode(it.id) },
                                onPodcastClicked = { viewModel.openPodcastDetail(it) },
                                onToggleSubscribe = { viewModel.toggleSubscription(it) }
                            )
                        }
                        3 -> {
                            ProfileScreen(
                                currentUser = currentUser,
                                favoritePodcasts = favoritePodcasts,
                                listeningHistory = listeningHistory,
                                subscribedCount = subscribedPodcasts.size,
                                currentPlayingEpisodeId = playerState.currentEpisode?.id,
                                isPlaying = playerState.isPlaying,
                                onPlayEpisode = { viewModel.playEpisode(it) },
                                onToggleQueue = { viewModel.toggleQueue(it.id) },
                                onToggleDownload = { viewModel.toggleDownload(it.id) },
                                onToggleFavoriteEpisode = { viewModel.toggleFavoriteEpisode(it.id) },
                                onPodcastClicked = { viewModel.openPodcastDetail(it) },
                                onToggleFavoritePodcast = { viewModel.toggleFavoritePodcast(it) },
                                onOpenAuthDialog = { viewModel.showAuthDialog() }
                            )
                        }
                    }
                }
            }
        }

        // Full Player Modal
        AnimatedVisibility(
            visible = isFullPlayerVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            FullPlayerBottomSheet(
                playerState = playerState,
                onDismiss = { viewModel.hideFullPlayer() },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onRewind10 = { viewModel.rewind10() },
                onForward30 = { viewModel.forward30() },
                onSeekTo = { viewModel.seekTo(it) },
                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                onToggleTrimSilence = { viewModel.toggleTrimSilence() },
                onSetSleepTimer = { viewModel.setSleepTimer(it) }
            )
        }

        // Auth & Profile Dialog
        if (isAuthDialogVisible) {
            AuthDialog(
                currentUser = currentUser,
                allUsers = allUsers,
                onDismiss = { viewModel.hideAuthDialog() },
                onSwitchUser = { viewModel.switchUser(it) },
                onLogin = { email, password, onResult ->
                    viewModel.loginUser(email, password, onResult)
                },
                onRegister = { username, email, fullName, password, onResult ->
                    viewModel.registerUser(username, email, fullName, password, onResult)
                },
                onNavigateToProfileTab = {
                    viewModel.selectTab(3)
                }
            )
        }
    }
}

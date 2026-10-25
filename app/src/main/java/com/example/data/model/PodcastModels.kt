package com.example.data.model

data class Podcast(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val totalEpisodes: Int = 12,
    val subscriberCount: Int = 12000,
    val isSubscribed: Boolean = false,
    val isFavorited: Boolean = false,
    val rating: Double = 4.8
)

data class Episode(
    val id: String,
    val podcastId: String,
    val podcastTitle: String,
    val podcastImageUrl: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val releaseDate: String,
    val isPlayed: Boolean = false,
    val playbackPositionSeconds: Int = 0,
    val isDownloaded: Boolean = false,
    val isFavorited: Boolean = false,
    val isInQueue: Boolean = false
) {
    val durationFormatted: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return if (mins >= 60) {
                val hours = mins / 60
                val remMins = mins % 60
                "${hours} sa ${remMins} dk"
            } else {
                "${mins} dk"
            }
        }

    val remainingFormatted: String
        get() {
            val remaining = (durationSeconds - playbackPositionSeconds).coerceAtLeast(0)
            val mins = remaining / 60
            return if (mins > 0) "${mins} dk kaldı" else "Tamamlandı"
        }
}

data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarColorHex: String = "#1A73E8",
    val createdAt: Long = System.currentTimeMillis(),
    val totalListeningMinutes: Int = 184
)

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val podcastCount: Int
)

data class ListeningHistoryItem(
    val id: Long = 0,
    val userId: String,
    val episodeId: String,
    val episodeTitle: String,
    val podcastTitle: String,
    val podcastImageUrl: String,
    val listenedAt: Long,
    val progressSeconds: Int,
    val durationSeconds: Int
)

data class PlayerState(
    val currentEpisode: Episode? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isTrimSilence: Boolean = false,
    val sleepTimerMinutesRemaining: Int? = null
)

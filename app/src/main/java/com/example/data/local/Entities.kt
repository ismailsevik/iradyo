package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
data class PodcastEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: String,
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
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarColorHex: String = "#1A73E8",
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val totalListeningMinutes: Int = 184
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "podcastId"])
data class FavoriteEntity(
    val userId: String,
    val podcastId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions", primaryKeys = ["userId", "podcastId"])
data class SubscriptionEntity(
    val userId: String,
    val podcastId: String,
    val notifyNewEpisodes: Boolean = true,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val episodeId: String,
    val listenedAt: Long = System.currentTimeMillis(),
    val progressSeconds: Int = 0,
    val completed: Boolean = false
)

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey val episodeId: String,
    val queueOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

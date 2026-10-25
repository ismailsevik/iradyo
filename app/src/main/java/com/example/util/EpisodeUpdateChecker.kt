package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.PodcastDao
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.data.remote.PodcastRssClient
import com.example.player.MediaNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object EpisodeUpdateChecker {

    suspend fun checkSubscribedPodcastsForNewEpisodes(
        context: Context,
        podcastDao: PodcastDao
    ): Int = withContext(Dispatchers.IO) {
        var newEpisodesCount = 0
        try {
            val subscriptions = podcastDao.getSubscribedPodcasts().first()
            for (podcastEntity in subscriptions) {
                val feedUrl = extractFeedUrl(podcastEntity.description)
                if (feedUrl.isBlank()) continue

                val podcastDomain = Podcast(
                    id = podcastEntity.id,
                    title = podcastEntity.title,
                    author = podcastEntity.author,
                    description = podcastEntity.description,
                    imageUrl = podcastEntity.imageUrl,
                    category = podcastEntity.category,
                    totalEpisodes = podcastEntity.totalEpisodes,
                    subscriberCount = podcastEntity.subscriberCount,
                    isSubscribed = podcastEntity.isSubscribed,
                    isFavorited = podcastEntity.isFavorited,
                    rating = podcastEntity.rating
                )

                val onlineEpisodes = PodcastRssClient.fetchEpisodesForFeed(podcastDomain, feedUrl)
                if (onlineEpisodes.isEmpty()) continue

                val localEpisodes = podcastDao.getEpisodesForPodcast(podcastEntity.id).first()
                val localTitles = localEpisodes.map { it.title.trim().lowercase() }.toSet()
                val localAudioUrls = localEpisodes.map { it.audioUrl.trim() }.toSet()

                val brandNew = onlineEpisodes.filter { ep ->
                    !localTitles.contains(ep.title.trim().lowercase()) &&
                    !localAudioUrls.contains(ep.audioUrl.trim())
                }

                if (brandNew.isNotEmpty()) {
                    podcastDao.insertEpisodes(brandNew.map { ep ->
                        com.example.data.local.EpisodeEntity(
                            id = ep.id,
                            podcastId = ep.podcastId,
                            podcastTitle = ep.podcastTitle,
                            podcastImageUrl = ep.podcastImageUrl,
                            title = ep.title,
                            description = ep.description,
                            audioUrl = ep.audioUrl,
                            durationSeconds = ep.durationSeconds,
                            releaseDate = ep.releaseDate,
                            isPlayed = false,
                            playbackPositionSeconds = 0,
                            isDownloaded = false,
                            isFavorited = false,
                            isInQueue = false
                        )
                    })
                    newEpisodesCount += brandNew.size

                    // Trigger immediate system notification for new episode
                    val latest = brandNew.first()
                    MediaNotificationManager.showNewEpisodeNotification(
                        context = context,
                        podcastTitle = podcastEntity.title,
                        episodeTitle = latest.title
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EpisodeUpdateChecker", "Error checking for new episodes", e)
        }
        newEpisodesCount
    }

    private fun extractFeedUrl(desc: String): String {
        return if (desc.contains("RSS:") || desc.contains("RSS Kaynağı:")) {
            val part = desc.substringAfter("RSS").substringAfter(":")
            part.trim().split(" ").firstOrNull() ?: ""
        } else if (desc.startsWith("http")) {
            desc.trim()
        } else ""
    }
}

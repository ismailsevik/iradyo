package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts")
    fun getAllPodcasts(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts WHERE id = :podcastId")
    suspend fun getPodcastById(podcastId: String): PodcastEntity?

    @Query("SELECT * FROM podcasts WHERE isSubscribed = 1")
    fun getSubscribedPodcasts(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts WHERE category = :category")
    fun getPodcastsByCategory(category: String): Flow<List<PodcastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcasts(podcasts: List<PodcastEntity>)

    @Update
    suspend fun updatePodcast(podcast: PodcastEntity)

    @Query("UPDATE podcasts SET isSubscribed = :isSubscribed WHERE id = :podcastId")
    suspend fun updateSubscription(podcastId: String, isSubscribed: Boolean)

    @Query("UPDATE podcasts SET isFavorited = :isFavorited WHERE id = :podcastId")
    suspend fun updateFavorite(podcastId: String, isFavorited: Boolean)

    // Episodes
    @Query("SELECT * FROM episodes ORDER BY id ASC")
    fun getAllEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId")
    fun getEpisodesForPodcast(podcastId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: String): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE isInQueue = 1")
    fun getQueuedEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE isDownloaded = 1")
    fun getDownloadedEpisodes(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Query("UPDATE episodes SET playbackPositionSeconds = :position, isPlayed = :isPlayed WHERE id = :episodeId")
    suspend fun updatePlaybackPosition(episodeId: String, position: Int, isPlayed: Boolean)

    @Query("UPDATE episodes SET isInQueue = :isInQueue WHERE id = :episodeId")
    suspend fun updateQueueStatus(episodeId: String, isInQueue: Boolean)

    @Query("UPDATE episodes SET isDownloaded = :isDownloaded WHERE id = :episodeId")
    suspend fun updateDownloadStatus(episodeId: String, isDownloaded: Boolean)

    @Query("UPDATE episodes SET isFavorited = :isFavorited WHERE id = :episodeId")
    suspend fun updateEpisodeFavorite(episodeId: String, isFavorited: Boolean)

    @Query("SELECT * FROM episodes WHERE isFavorited = 1")
    fun getFavoriteEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchEpisodes(query: String): Flow<List<EpisodeEntity>>

    @Query("UPDATE podcasts SET isSubscribed = 0, isFavorited = 0")
    suspend fun resetPodcastsUserData()

    @Query("UPDATE episodes SET isDownloaded = 0, isFavorited = 0, isInQueue = 0, playbackPositionSeconds = 0, isPlayed = 0")
    suspend fun resetEpisodesUserData()

    @Query("DELETE FROM podcasts")
    suspend fun deleteAllPodcasts()

    @Query("DELETE FROM episodes")
    suspend fun deleteAllEpisodes()

    @Query("DELETE FROM podcasts WHERE id = :podcastId")
    suspend fun deletePodcastById(podcastId: String)

    @Query("DELETE FROM episodes WHERE podcastId = :podcastId")
    suspend fun deleteEpisodesByPodcastId(podcastId: String)

    @Query("UPDATE episodes SET durationSeconds = :duration WHERE id = :episodeId")
    suspend fun updateEpisodeDuration(episodeId: String, duration: Int)
}

package com.example.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object PodcastDownloadHelper {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // In-memory set of currently downloading episode IDs
    private val downloadingEpisodes = ConcurrentHashMap.newKeySet<String>()

    fun isEpisodeDownloading(episodeId: String): Boolean = downloadingEpisodes.contains(episodeId)

    fun getPodcastlerDirectory(context: Context): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS) ?: context.filesDir
        val podcastlerDir = File(baseDir, "Podcastler")
        if (!podcastlerDir.exists()) {
            podcastlerDir.mkdirs()
        }
        return podcastlerDir
    }

    fun getLocalAudioFile(context: Context, episodeId: String): File {
        val sanitizedId = episodeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val dir = getPodcastlerDirectory(context)
        return File(dir, "ep_$sanitizedId.mp3")
    }

    fun isDownloadedLocally(context: Context, episodeId: String): Boolean {
        val file = getLocalAudioFile(context, episodeId)
        return file.exists() && file.length() > 1024L
    }

    suspend fun downloadEpisode(
        context: Context,
        episode: Episode,
        onProgress: ((Int) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (episode.audioUrl.isBlank()) return@withContext false
        val file = getLocalAudioFile(context, episode.id)

        try {
            downloadingEpisodes.add(episode.id)
            val request = Request.Builder()
                .url(episode.audioUrl)
                .header("User-Agent", "iRadyo-Podcast-Downloader/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("PodcastDownloader", "Download failed with code ${response.code}")
                downloadingEpisodes.remove(episode.id)
                return@withContext false
            }

            val body = response.body ?: run {
                downloadingEpisodes.remove(episode.id)
                return@withContext false
            }

            val totalBytes = body.contentLength()
            val tempFile = File(file.parentFile, "${file.name}.tmp")

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastPercent = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val percent = ((totalRead * 100) / totalBytes).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress?.invoke(percent)
                            }
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                if (file.exists()) {
                    file.delete()
                }
                val renamed = tempFile.renameTo(file)
                downloadingEpisodes.remove(episode.id)
                return@withContext renamed
            } else {
                tempFile.delete()
                downloadingEpisodes.remove(episode.id)
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("PodcastDownloader", "Error downloading episode ${episode.id}", e)
            downloadingEpisodes.remove(episode.id)
            try {
                val tempFile = File(file.parentFile, "${file.name}.tmp")
                if (tempFile.exists()) tempFile.delete()
            } catch (_: Exception) {}
            return@withContext false
        }
    }

    suspend fun deleteDownloadedEpisode(context: Context, episodeId: String): Boolean = withContext(Dispatchers.IO) {
        val file = getLocalAudioFile(context, episodeId)
        if (file.exists()) {
            file.delete()
        } else false
    }

    suspend fun clearAllLocalDownloads(context: Context) = withContext(Dispatchers.IO) {
        val podcastlerDir = getPodcastlerDirectory(context)
        podcastlerDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".mp3") || file.name.endsWith(".tmp")) {
                file.delete()
            }
        }
        val legacyDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS) ?: context.filesDir
        legacyDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".mp3") || file.name.endsWith(".tmp")) {
                file.delete()
            }
        }
    }
}

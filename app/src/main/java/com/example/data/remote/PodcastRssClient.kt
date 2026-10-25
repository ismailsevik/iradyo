package com.example.data.remote

import android.util.Log
import android.util.Xml
import com.example.data.model.Episode
import com.example.data.model.Podcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object PodcastRssClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchPodcasts(query: String): List<Podcast> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encoded&entity=podcast&country=TR&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "iRadyo-Android/3.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return@withContext emptyList()
            val list = mutableListOf<Podcast>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val id = item.optLong("collectionId", 0L).toString()
                val title = item.optString("collectionName", "Bilinmeyen Podcast")
                val author = item.optString("artistName", "Bilinmeyen Yayıncı")
                val imageUrl = item.optString("artworkUrl600", item.optString("artworkUrl100", ""))
                val category = item.optString("primaryGenreName", "Genel")
                val feedUrl = item.optString("feedUrl", "")

                if (feedUrl.isNotBlank() && id.isNotBlank()) {
                    list.add(
                        Podcast(
                            id = id,
                            title = title,
                            author = author,
                            description = "RSS Kaynağı: $feedUrl",
                            imageUrl = if (imageUrl.isNotBlank()) imageUrl else "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600",
                            category = category,
                            totalEpisodes = item.optInt("trackCount", 24),
                            subscriberCount = (1000..95000).random(),
                            isSubscribed = false,
                            isFavorited = false,
                            rating = 4.8
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e("PodcastRssClient", "Error searching podcasts", e)
            emptyList()
        }
    }

    suspend fun fetchEpisodesForFeed(podcast: Podcast, feedUrl: String): List<Episode> = withContext(Dispatchers.IO) {
        if (feedUrl.isBlank()) return@withContext emptyList()
        try {
            val request = Request.Builder()
                .url(feedUrl)
                .header("User-Agent", "Mozilla/5.0 iRadyo-Android/3.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val xmlData = response.body?.string() ?: return@withContext emptyList()

            parseRssXml(podcast, xmlData)
        } catch (e: Exception) {
            Log.e("PodcastRssClient", "Error fetching feed for ${podcast.title}", e)
            emptyList()
        }
    }

    private fun parseRssXml(podcast: Podcast, xmlData: String): List<Episode> {
        val episodes = mutableListOf<Episode>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlData))

            var eventType = parser.eventType
            var inItem = false
            var currentTitle = ""
            var currentDesc = ""
            var currentAudioUrl = ""
            var currentPubDate = ""
            var currentDuration = 1800 // default 30 min

            while (eventType != XmlPullParser.END_DOCUMENT && episodes.size < 30) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tag.equals("item", ignoreCase = true)) {
                            inItem = true
                            currentTitle = ""
                            currentDesc = ""
                            currentAudioUrl = ""
                            currentPubDate = ""
                            currentDuration = 1800
                        } else if (inItem) {
                            when (tag.lowercase()) {
                                "title" -> currentTitle = parser.nextText() ?: ""
                                "description" -> currentDesc = cleanHtml(parser.nextText() ?: "")
                                "pubdate" -> currentPubDate = formatPubDate(parser.nextText() ?: "")
                                "itunes:duration" -> {
                                    val durText = parser.nextText() ?: ""
                                    currentDuration = parseDuration(durText)
                                }
                                "enclosure" -> {
                                    val url = parser.getAttributeValue(null, "url") ?: ""
                                    val type = parser.getAttributeValue(null, "type") ?: ""
                                    if (url.isNotBlank() && (type.contains("audio", true) || url.endsWith(".mp3", true) || url.contains(".mp3") || url.contains("audio"))) {
                                        currentAudioUrl = url
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag.equals("item", ignoreCase = true)) {
                            inItem = false
                            if (currentTitle.isNotBlank() && currentAudioUrl.isNotBlank()) {
                                episodes.add(
                                    Episode(
                                        id = "ep_${podcast.id}_${episodes.size + 1}",
                                        podcastId = podcast.id,
                                        podcastTitle = podcast.title,
                                        podcastImageUrl = podcast.imageUrl,
                                        title = currentTitle.trim(),
                                        description = if (currentDesc.isNotBlank()) currentDesc.trim() else "Bölüm açıklaması bulunmuyor.",
                                        audioUrl = currentAudioUrl.trim(),
                                        durationSeconds = currentDuration,
                                        releaseDate = if (currentPubDate.isNotBlank()) currentPubDate else "Yeni",
                                        isPlayed = false,
                                        playbackPositionSeconds = 0,
                                        isDownloaded = false,
                                        isFavorited = false,
                                        isInQueue = false
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("PodcastRssClient", "XML parsing error", e)
        }
        return episodes
    }

    private fun cleanHtml(html: String): String {
        return html.replace(Regex("<.*?>"), "").replace("&nbsp;", " ").replace("&amp;", "&").take(250)
    }

    private fun formatPubDate(pubDate: String): String {
        return try {
            val parts = pubDate.split(" ")
            if (parts.size >= 4) {
                "${parts[1]} ${parts[2]} ${parts[3]}"
            } else pubDate.take(16)
        } catch (_: Exception) {
            pubDate.take(16)
        }
    }

    private fun parseDuration(duration: String): Int {
        return try {
            if (duration.contains(":")) {
                val parts = duration.split(":")
                if (parts.size == 3) {
                    parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt()
                } else if (parts.size == 2) {
                    parts[0].toInt() * 60 + parts[1].toInt()
                } else 1800
            } else {
                duration.toIntOrNull() ?: 1800
            }
        } catch (_: Exception) {
            1800
        }
    }
}

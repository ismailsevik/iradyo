package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.data.model.Episode
import com.example.data.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerManager(
    private val context: Context,
    private val onProgressUpdate: (episodeId: String, positionSec: Int, isCompleted: Boolean) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentPlayingEpisode: Episode? = null
    private var queueEpisodes: List<Episode> = emptyList()

    fun updateQueue(episodes: List<Episode>) {
        queueEpisodes = episodes
    }

    fun playEpisode(episode: Episode) {
        val sameEpisode = currentPlayingEpisode?.id == episode.id
        if (sameEpisode && mediaPlayer != null) {
            if (!_playerState.value.isPlaying) {
                resume()
            }
            return
        }

        currentPlayingEpisode = episode
        _playerState.update {
            it.copy(
                currentEpisode = episode,
                isBuffering = true,
                isPlaying = false,
                currentPositionSeconds = episode.playbackPositionSeconds,
                durationSeconds = episode.durationSeconds
            )
        }

        releasePlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(episode.audioUrl)
                setOnPreparedListener { mp ->
                    _playerState.update { it.copy(isBuffering = false, isPlaying = true) }
                    val startPosMs = (episode.playbackPositionSeconds * 1000).coerceAtLeast(0)
                    if (startPosMs > 0 && startPosMs < mp.duration) {
                        mp.seekTo(startPosMs)
                    }
                    applySpeed(_playerState.value.playbackSpeed)
                    mp.start()
                    startProgressTracker()
                }
                setOnCompletionListener {
                    handlePlaybackCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error during playback: what=$what extra=$extra")
                    _playerState.update { it.copy(isBuffering = false, isPlaying = false) }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to start media player", e)
            _playerState.update { it.copy(isBuffering = false, isPlaying = false) }
        }
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        _playerState.update { it.copy(isPlaying = false) }
        currentPlayingEpisode?.let { ep ->
            onProgressUpdate(ep.id, _playerState.value.currentPositionSeconds, false)
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _playerState.update { it.copy(isPlaying = true) }
            startProgressTracker()
        } ?: run {
            currentPlayingEpisode?.let { playEpisode(it) }
        }
    }

    fun rewind10Seconds() {
        val newPos = (_playerState.value.currentPositionSeconds - 10).coerceAtLeast(0)
        seekToSeconds(newPos)
    }

    fun forward30Seconds() {
        val total = _playerState.value.durationSeconds
        val newPos = (_playerState.value.currentPositionSeconds + 30).coerceAtMost(total)
        seekToSeconds(newPos)
    }

    fun seekToSeconds(seconds: Int) {
        mediaPlayer?.seekTo(seconds * 1000)
        _playerState.update { it.copy(currentPositionSeconds = seconds) }
        currentPlayingEpisode?.let { ep ->
            onProgressUpdate(ep.id, seconds, false)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.update { it.copy(playbackSpeed = speed) }
        applySpeed(speed)
    }

    fun toggleTrimSilence() {
        _playerState.update { it.copy(isTrimSilence = !it.isTrimSilence) }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _playerState.update { it.copy(sleepTimerMinutesRemaining = minutes) }

        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch {
                var remaining = minutes
                while (isActive && remaining > 0) {
                    delay(60000L) // 1 minute
                    remaining -= 1
                    _playerState.update { it.copy(sleepTimerMinutesRemaining = remaining) }
                }
                if (isActive) {
                    pause()
                    _playerState.update { it.copy(sleepTimerMinutesRemaining = null) }
                }
            }
        }
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.playbackParams = PlaybackParams().apply { this.speed = speed }
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Could not apply speed $speed", e)
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playerState.value.isPlaying) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val posSec = mp.currentPosition / 1000
                        val durSec = if (mp.duration > 0) mp.duration / 1000 else _playerState.value.durationSeconds
                        _playerState.update {
                            it.copy(currentPositionSeconds = posSec, durationSeconds = durSec)
                        }
                        currentPlayingEpisode?.let { ep ->
                            onProgressUpdate(ep.id, posSec, false)
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun handlePlaybackCompleted() {
        _playerState.update { it.copy(isPlaying = false, currentPositionSeconds = it.durationSeconds) }
        currentPlayingEpisode?.let { ep ->
            onProgressUpdate(ep.id, ep.durationSeconds, true)
        }
        // Auto play next from queue if exists
        val nextInQueue = queueEpisodes.firstOrNull { it.id != currentPlayingEpisode?.id }
        if (nextInQueue != null) {
            playEpisode(nextInQueue)
        }
    }

    fun release() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}

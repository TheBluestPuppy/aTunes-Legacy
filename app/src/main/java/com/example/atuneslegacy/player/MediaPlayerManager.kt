package com.example.atuneslegacy.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MediaType {
    MUSIC, MUSIC_VIDEO, TV_SHOW, MOVIE, PODCAST, AUDIOBOOK
}

data class MediaItemMetadata(
    val id: String,
    val title: String,
    val subtitle: String,
    val albumOrCategory: String = "",
    val mediaUri: String,
    val artworkUri: String = "",
    val isVideo: Boolean = false,
    val mediaType: MediaType = MediaType.MUSIC
)

class MediaPlayerManager(context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentMedia = MutableStateFlow<MediaItemMetadata?>(null)
    val currentMedia: StateFlow<MediaItemMetadata?> = _currentMedia.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playlist = MutableStateFlow<List<MediaItemMetadata>>(emptyList())
    val playlist: StateFlow<List<MediaItemMetadata>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _showVideoPlayer = MutableStateFlow(false)
    val showVideoPlayer: StateFlow<Boolean> = _showVideoPlayer.asStateFlow()

    private val _showMusicPlayer = MutableStateFlow(false)
    val showMusicPlayer: StateFlow<Boolean> = _showMusicPlayer.asStateFlow()

    fun showMusicPlayer() {
        if (_currentMedia.value != null && !_currentMedia.value!!.isVideo) {
            _showMusicPlayer.value = true
        }
    }

    fun hideMusicPlayer() {
        _showMusicPlayer.value = false
    }

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
            }
        })
    }

    fun playMedia(mediaItem: MediaItemMetadata, playlist: List<MediaItemMetadata> = listOf(mediaItem)) {
        _playlist.value = playlist
        val index = playlist.indexOfFirst { it.id == mediaItem.id }
        _currentIndex.value = if (index != -1) index else 0

        _currentMedia.value = mediaItem
        _showVideoPlayer.value = mediaItem.isVideo

        try {
            val uri = Uri.parse(mediaItem.mediaUri)
            val exoMediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(exoMediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (_currentMedia.value != null) {
                exoPlayer.play()
            }
        }
    }

    fun playNext() {
        val currentList = _playlist.value
        val nextIdx = _currentIndex.value + 1
        if (currentList.isNotEmpty() && nextIdx < currentList.size) {
            playMedia(currentList[nextIdx], currentList)
        }
    }

    fun playPrevious() {
        val currentList = _playlist.value
        val prevIdx = _currentIndex.value - 1
        if (currentList.isNotEmpty() && prevIdx >= 0) {
            playMedia(currentList[prevIdx], currentList)
        } else {
            exoPlayer.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun hideVideoPlayer() {
        _showVideoPlayer.value = false
    }

    fun openVideoPlayer() {
        if (_currentMedia.value?.isVideo == true) {
            _showVideoPlayer.value = true
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdate()
        exoPlayer.release()
    }
}

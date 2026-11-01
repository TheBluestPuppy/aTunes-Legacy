package com.example.atuneslegacy.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
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

@UnstableApi
class MediaPlayerManager(context: Context) {
    private val appContext = context.applicationContext

    private val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
    private val controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()

    private var mediaController: MediaController? = null

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

    val exoPlayer: Player?
        get() = mediaController

    init {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(object : Player.Listener {
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
                        _durationMs.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                    } else if (playbackState == Player.STATE_ENDED) {
                        playNext()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _isPlaying.value = false
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun showMusicPlayer() {
        if (_currentMedia.value != null && !_currentMedia.value!!.isVideo) {
            _showMusicPlayer.value = true
        }
    }

    fun hideMusicPlayer() {
        _showMusicPlayer.value = false
    }

    fun playMedia(mediaItem: MediaItemMetadata, playlist: List<MediaItemMetadata> = listOf(mediaItem)) {
        _playlist.value = playlist
        val index = playlist.indexOfFirst { it.id == mediaItem.id }
        _currentIndex.value = if (index != -1) index else 0

        _currentMedia.value = mediaItem
        _showVideoPlayer.value = mediaItem.isVideo

        try {
            val uri = Uri.parse(mediaItem.mediaUri)
            val metadata = MediaMetadata.Builder()
                .setTitle(mediaItem.title)
                .setArtist(mediaItem.subtitle)
                .setAlbumTitle(mediaItem.albumOrCategory)
                .setArtworkUri(if (mediaItem.artworkUri.isNotBlank()) Uri.parse(mediaItem.artworkUri) else null)
                .build()
            val exoMediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()

            mediaController?.apply {
                setMediaItem(exoMediaItem)
                prepare()
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                if (_currentMedia.value != null) {
                    controller.play()
                }
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
            mediaController?.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
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
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                        _durationMs.value = controller.duration.coerceAtLeast(0L)
                    }
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
        MediaController.releaseFuture(controllerFuture)
        mediaController = null
    }
}

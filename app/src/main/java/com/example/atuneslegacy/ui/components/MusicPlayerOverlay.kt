package com.example.atuneslegacy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.atuneslegacy.player.MediaPlayerManager
import com.example.atuneslegacy.player.MediaType
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun MusicPlayerOverlay(
    mediaPlayerManager: MediaPlayerManager,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val currentMedia by mediaPlayerManager.currentMedia.collectAsState()
    val isPlaying by mediaPlayerManager.isPlaying.collectAsState()
    val currentPositionMs by mediaPlayerManager.currentPositionMs.collectAsState()
    val durationMs by mediaPlayerManager.durationMs.collectAsState()

    val playlist by mediaPlayerManager.playlist.collectAsState()
    val currentIndex by mediaPlayerManager.currentIndex.collectAsState()

    val isPodcast = currentMedia?.mediaType == MediaType.PODCAST || currentMedia?.albumOrCategory == "Podcast"
    val isAudiobook = currentMedia?.mediaType == MediaType.AUDIOBOOK || currentMedia?.albumOrCategory == "Audiobook"
    val isPodcastOrAudiobook = isPodcast || isAudiobook

    val podcastShows by viewModel.allPodcastShows.collectAsState()
    val allPodcastEpisodes by viewModel.mediaRepository.getAllPodcastEpisodes().collectAsState(initial = emptyList())

    val podcastShow = remember(currentMedia, podcastShows) {
        if (isPodcast) {
            podcastShows.firstOrNull {
                it.title.equals(currentMedia?.subtitle, ignoreCase = true) ||
                it.title.equals(currentMedia?.albumOrCategory, ignoreCase = true)
            }
        } else null
    }

    val podcastLogoUri = podcastShow?.logoThumbnailUri ?: ""
    val podcastDescription = podcastShow?.description ?: "No description available for this podcast show."

    var isFlipped by remember { mutableStateOf(false) }
    var showQueueDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val animatedRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            viewModel = viewModel,
            currentMedia = currentMedia,
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Now Playing Screen Content
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = themeColors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (themeColors.isAppleMusic || isPodcastOrAudiobook) Icons.Default.KeyboardArrowDown else Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.textPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = if (isAudiobook) "Audiobook" else if (isPodcast) "Podcast Episode" else (if (themeColors.isAppleMusic) "" else "Now Playing"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isPodcastOrAudiobook) {
                            IconButton(
                                onClick = { showAddToPlaylistDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "Add to Playlist",
                                    tint = themeColors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (!isAudiobook) {
                            IconButton(
                                onClick = { showQueueDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "Queue",
                                    tint = themeColors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(36.dp))
                        }
                    }
                }

                // Artwork / Podcast Cover with Double-Tap Flip 3D animation (ONLY for podcasts)
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            if (isPodcast) {
                                rotationY = animatedRotation
                                cameraDistance = 12f * density
                            }
                        }
                        .then(
                            if (isPodcast) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { isFlipped = !isFlipped }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.surface)
                        .border(0.5.dp, themeColors.surfaceBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPodcast || animatedRotation <= 90f) {
                        if (isPodcast && podcastLogoUri.isNotBlank()) {
                            AsyncImage(
                                model = podcastLogoUri,
                                contentDescription = podcastShow?.title ?: "Podcast Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (currentMedia?.artworkUri?.isNotBlank() == true) {
                            AsyncImage(
                                model = currentMedia?.artworkUri,
                                contentDescription = currentMedia?.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = if (isPodcast) Icons.Default.Podcasts else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(themeColors.surface)
                                .padding(16.dp)
                                .graphicsLayer { rotationY = 180f },
                            contentAlignment = Alignment.Center
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = podcastShow?.title ?: "Podcast Description",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = podcastDescription,
                                    fontSize = 12.sp,
                                    color = themeColors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Title & Author/Podcast Show Name & Add to Playlist Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = if (themeColors.isAppleMusic || isPodcastOrAudiobook) Alignment.Start else Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentMedia?.title ?: "No Media Playing",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentMedia?.subtitle ?: "Artist",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.primaryAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isPodcastOrAudiobook) {
                        OutlinedButton(
                            onClick = { showAddToPlaylistDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = themeColors.surface),
                            border = BorderStroke(1.dp, themeColors.primaryAccent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "Add to Playlist",
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to Playlist", fontSize = 11.sp, color = themeColors.primaryAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Progress Scrubber
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Slider(
                        value = if (durationMs > 0) currentPositionMs.toFloat() else 0f,
                        onValueChange = { newVal ->
                            mediaPlayerManager.seekTo(newVal.toLong())
                        },
                        valueRange = 0f..(durationMs.coerceAtLeast(1L).toFloat()),
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.primaryAccent,
                            activeTrackColor = themeColors.primaryAccent,
                            inactiveTrackColor = themeColors.surfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(currentPositionMs),
                            fontSize = 11.sp,
                            color = themeColors.textSecondary
                        )
                        Text(
                            text = formatDuration(durationMs),
                            fontSize = 11.sp,
                            color = themeColors.textSecondary
                        )
                    }
                }

                // Podcast & Audiobook 15s Skip / Transport Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPodcastOrAudiobook) {
                        val showEpisodes = if (isPodcast) {
                            allPodcastEpisodes.filter { it.podcastName.equals(podcastShow?.title ?: currentMedia?.subtitle, ignoreCase = true) }
                                .sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
                        } else emptyList()
                        val currentEpIndex = if (isPodcast) showEpisodes.indexOfFirst { it.uri == currentMedia?.mediaUri || it.title == currentMedia?.title } else -1

                        // Previous Track / Episode Button
                        IconButton(
                            onClick = {
                                if (isPodcast && currentEpIndex > 0) {
                                    val prevEp = showEpisodes[currentEpIndex - 1]
                                    viewModel.playPodcastEpisode(prevEp)
                                } else {
                                    mediaPlayerManager.playPrevious()
                                }
                            },
                            enabled = if (isPodcast) currentEpIndex > 0 else true,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // -15s Skip Backward
                        OutlinedButton(
                            onClick = { mediaPlayerManager.seekTo((currentPositionMs - 15000L).coerceAtLeast(0L)) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("-15s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Play/Pause
                        IconButton(
                            onClick = { mediaPlayerManager.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(themeColors.primaryAccent, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // +15s Skip Forward
                        OutlinedButton(
                            onClick = { mediaPlayerManager.seekTo((currentPositionMs + 15000L).coerceAtMost(durationMs)) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("+15s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Next Track / Episode Button
                        IconButton(
                            onClick = {
                                if (isPodcast && currentEpIndex != -1 && currentEpIndex < showEpisodes.size - 1) {
                                    val nextEp = showEpisodes[currentEpIndex + 1]
                                    viewModel.playPodcastEpisode(nextEp)
                                } else {
                                    mediaPlayerManager.playNext()
                                }
                            },
                            enabled = if (isPodcast) (currentEpIndex != -1 && currentEpIndex < showEpisodes.size - 1) else true,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { mediaPlayerManager.playPrevious() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { mediaPlayerManager.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(themeColors.primaryAccent, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { mediaPlayerManager.playNext() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // iOS 6 Style Slide-Up Queue Sheet Overlay
        AnimatedVisibility(
            visible = showQueueDialog,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .zIndex(200f)
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color(0xFF1C1E24),
                border = BorderStroke(1.dp, Color(0xFF3A3E47)),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Bar (iOS 6 style)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Up Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Button(
                            onClick = { showQueueDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Done", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFF2C303B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Queue List
                    if (playlist.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Queue is empty", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(playlist) { index, item ->
                                val isCurrent = index == currentIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isCurrent) Color(0xFF2B73E7).copy(alpha = 0.3f) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            mediaPlayerManager.playMedia(item, playlist)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) Color(0xFF388BFD) else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFF262A33), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddToPlaylistDialog(
    viewModel: MainViewModel,
    currentMedia: com.example.atuneslegacy.player.MediaItemMetadata?,
    onDismiss: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    val playlists by viewModel.allPlaylists.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .zIndex(300f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1E24),
            border = BorderStroke(1.dp, Color(0xFF3A3E47)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .width(320.dp)
                .height(360.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Playlist",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6CE5)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Done", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF2C303B), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Playlists Available", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.addCurrentMediaToPlaylist(playlist.id, currentMedia)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playlist.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            HorizontalDivider(color = Color(0xFF262A33), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

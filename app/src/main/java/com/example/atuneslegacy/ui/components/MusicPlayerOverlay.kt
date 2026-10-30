package com.example.atuneslegacy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.player.MediaPlayerManager
import com.example.atuneslegacy.player.MediaType
import com.example.atuneslegacy.ui.theme.LocalThemeColors

@Composable
fun MusicPlayerOverlay(
    mediaPlayerManager: MediaPlayerManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val currentMedia by mediaPlayerManager.currentMedia.collectAsState()
    val isPlaying by mediaPlayerManager.isPlaying.collectAsState()
    val currentPositionMs by mediaPlayerManager.currentPositionMs.collectAsState()
    val durationMs by mediaPlayerManager.durationMs.collectAsState()

    val isPodcast = currentMedia?.mediaType == MediaType.PODCAST || currentMedia?.albumOrCategory == "Podcast"
    var speedIndex by remember { mutableIntStateOf(0) }
    val speeds = listOf(1f, 1.25f, 1.5f, 2f)
    val speedLabels = listOf("1x", "1.25x", "1.5x", "2x")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = themeColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                        imageVector = if (themeColors.isAppleMusic || isPodcast) Icons.Default.KeyboardArrowDown else Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = if (isPodcast) "Podcast Episode" else (if (themeColors.isAppleMusic) "" else "Now Playing"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.size(36.dp))
            }

            // Artwork / Podcast Cover
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.surface)
                    .border(0.5.dp, themeColors.surfaceBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentMedia?.artworkUri?.isNotBlank() == true) {
                    AsyncImage(
                        model = currentMedia?.artworkUri,
                        contentDescription = currentMedia?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Title & Podcast Show Name
            Column(
                horizontalAlignment = if (themeColors.isAppleMusic || isPodcast) Alignment.Start else Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = currentMedia?.title ?: "No Episode Playing",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentMedia?.subtitle ?: "Podcast Show",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.primaryAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

            // Podcast 15s Skip / Music Transport Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPodcast) {
                    // -15s Skip Backward
                    OutlinedButton(
                        onClick = { mediaPlayerManager.seekTo((currentPositionMs - 15000L).coerceAtLeast(0L)) },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("-15s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                } else {
                    IconButton(
                        onClick = { mediaPlayerManager.playPrevious() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (themeColors.isAppleMusic) Icons.Default.SkipPrevious else Icons.Default.FastRewind,
                            contentDescription = "Previous",
                            tint = themeColors.textPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                }

                // Play / Pause
                IconButton(
                    onClick = { mediaPlayerManager.togglePlayPause() },
                    modifier = Modifier
                        .size(68.dp)
                        .background(if (themeColors.isAppleMusic && !isPodcast) Color.Transparent else themeColors.primaryAccent, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (themeColors.isAppleMusic && !isPodcast) themeColors.textPrimary else Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (isPodcast) {
                    Spacer(modifier = Modifier.width(20.dp))
                    // +15s Skip Forward
                    OutlinedButton(
                        onClick = { mediaPlayerManager.seekTo((currentPositionMs + 15000L).coerceAtMost(durationMs)) },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("+15s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                    IconButton(
                        onClick = { mediaPlayerManager.playNext() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (themeColors.isAppleMusic) Icons.Default.SkipNext else Icons.Default.FastForward,
                            contentDescription = "Next",
                            tint = themeColors.textPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Playback Speed Button for Podcasts
            if (isPodcast) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            speedIndex = (speedIndex + 1) % speeds.size
                            mediaPlayerManager.setPlaybackSpeed(speeds[speedIndex])
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Speed: ${speedLabels[speedIndex]}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.primaryAccent)
                    }
                }
            }

            // Bottom Action Bar
            if (themeColors.isAppleMusic || isPodcast) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download",
                            tint = themeColors.primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = themeColors.primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More",
                            tint = themeColors.primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

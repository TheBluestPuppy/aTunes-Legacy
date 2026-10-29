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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = themeColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(themeColors.surface, CircleShape)
                        .border(1.dp, themeColors.surfaceBorder, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Now Playing",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.size(36.dp))
            }

            // Artwork / Album Cover
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.surface)
                    .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(12.dp)),
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

            // Song Title & Artist
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentMedia?.title ?: "No Song Playing",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentMedia?.subtitle ?: "Unknown Artist",
                    fontSize = 14.sp,
                    color = themeColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress Bar / Scrubber
            Column(modifier = Modifier.fillMaxWidth()) {
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

            // Transport Controls (Prev, Play/Pause, Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { mediaPlayerManager.playPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Track",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(
                    onClick = { mediaPlayerManager.togglePlayPause() },
                    modifier = Modifier
                        .size(72.dp)
                        .background(themeColors.primaryAccent, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(
                    onClick = { mediaPlayerManager.playNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Track",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

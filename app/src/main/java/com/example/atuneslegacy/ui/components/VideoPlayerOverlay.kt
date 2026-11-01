package com.example.atuneslegacy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.atuneslegacy.player.MediaPlayerManager

@UnstableApi
@Composable
fun VideoPlayerOverlay(
    mediaPlayerManager: MediaPlayerManager,
    modifier: Modifier = Modifier
) {
    val currentMedia by mediaPlayerManager.currentMedia.collectAsState()
    val isPlaying by mediaPlayerManager.isPlaying.collectAsState()
    val currentPosition by mediaPlayerManager.currentPositionMs.collectAsState()
    val duration by mediaPlayerManager.durationMs.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
    ) {
        // Video View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = mediaPlayerManager.exoPlayer
                    useController = false // Custom classic iOS controller overlay
                }
            },
            update = { view ->
                view.player = mediaPlayerManager.exoPlayer
                view.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Classic iOS Video Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Top Bar (Glossy iOS Style)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E222A).copy(alpha = 0.9f), Color(0xFF101216).copy(alpha = 0.9f))
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Done Button (Glossy Blue)
                        Button(
                            onClick = { mediaPlayerManager.hideVideoPlayer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D6CE5)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Elapsed Time
                        Text(
                            text = formatDuration(currentPosition),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Top Scrubber Slider
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { newVal ->
                                mediaPlayerManager.seekTo(newVal.toLong())
                            },
                            valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF388BFD),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Remaining / Total Duration
                        Text(
                            text = formatDuration(duration),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Resize / Fullscreen Toggle Button
                        IconButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier
                                .background(Color(0xFF2C3038), RoundedCornerShape(6.dp))
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Toggle Aspect Ratio",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Video Title subtitle
                    currentMedia?.let { media ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = media.title,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Center / Bottom Floating Translucent Control Box (Classic iPod/iPhone style)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF181B20).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .width(340.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 5 Playback Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { mediaPlayerManager.playPrevious() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            IconButton(onClick = { mediaPlayerManager.seekTo((currentPosition - 10000L).coerceAtLeast(0L)) }) {
                                Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            IconButton(
                                onClick = { mediaPlayerManager.togglePlayPause() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            IconButton(onClick = { mediaPlayerManager.seekTo((currentPosition + 10000L).coerceAtMost(duration)) }) {
                                Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            IconButton(onClick = { mediaPlayerManager.playNext() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom Scrubbing Slider inside Control Box
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { newVal ->
                                mediaPlayerManager.seekTo(newVal.toLong())
                            },
                            valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF388BFD),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

package com.example.atuneslegacy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.atuneslegacy.player.MediaPlayerManager
import kotlinx.coroutines.delay

@UnstableApi
@Composable
fun VideoPlayerOverlay(
    mediaPlayerManager: MediaPlayerManager,
    modifier: Modifier = Modifier
) {
    val currentMedia by mediaPlayerManager.currentMedia.collectAsState()
    var tapIndicatorText by remember { mutableStateOf<String?>(null) }
    var isRightSideTap by remember { mutableStateOf(false) }

    LaunchedEffect(tapIndicatorText) {
        if (tapIndicatorText != null) {
            delay(800)
            tapIndicatorText = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = mediaPlayerManager.exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            update = { view ->
                view.player = mediaPlayerManager.exoPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        // Transparent Gesture Box for Double-Tap Rewind/Fast-Forward
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val isRight = offset.x >= (size.width / 2f)
                            val currentPos = mediaPlayerManager.exoPlayer.currentPosition
                            val duration = mediaPlayerManager.exoPlayer.duration.coerceAtLeast(0L)

                            if (isRight) {
                                val newPos = (currentPos + 5000L).coerceAtMost(duration)
                                mediaPlayerManager.seekTo(newPos)
                                isRightSideTap = true
                                tapIndicatorText = "+5s ⏭"
                            } else {
                                val newPos = (currentPos - 5000L).coerceAtLeast(0L)
                                mediaPlayerManager.seekTo(newPos)
                                isRightSideTap = false
                                tapIndicatorText = "⏮ -5s"
                            }
                        }
                    )
                }
        ) {
            // Visual indicator on double-tap
            AnimatedVisibility(
                visible = tapIndicatorText != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(if (isRightSideTap) Alignment.CenterEnd else Alignment.CenterStart)
            ) {
                tapIndicatorText?.let { text ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Header & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { mediaPlayerManager.hideVideoPlayer() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Video",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                currentMedia?.let { media ->
                    Text(
                        text = "${media.title} • ${media.subtitle}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

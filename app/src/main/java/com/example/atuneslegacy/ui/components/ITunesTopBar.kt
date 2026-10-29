package com.example.atuneslegacy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.atuneslegacy.player.MediaPlayerManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.theme.ATunesLegacyTheme
import com.example.atuneslegacy.ui.theme.ITunesLcdBackground
import com.example.atuneslegacy.ui.theme.ITunesLcdBorder
import com.example.atuneslegacy.ui.theme.ITunesLcdProgressFill
import com.example.atuneslegacy.ui.theme.ITunesLcdProgressTrack
import com.example.atuneslegacy.ui.theme.ITunesLcdTextPrimary
import com.example.atuneslegacy.ui.theme.ITunesLcdTextSecondary
import com.example.atuneslegacy.ui.theme.ITunesTopBarBorder
import com.example.atuneslegacy.ui.theme.ITunesTopBarEnd
import com.example.atuneslegacy.ui.theme.ITunesTopBarStart

@Composable
fun ITunesTopBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    mediaPlayerManager: MediaPlayerManager? = null,
    modifier: Modifier = Modifier
) {
    val currentMedia = mediaPlayerManager?.currentMedia?.collectAsState()?.value
    val isPlaying = mediaPlayerManager?.isPlaying?.collectAsState()?.value ?: false
    val currentPositionMs = mediaPlayerManager?.currentPositionMs?.collectAsState()?.value ?: 0L
    val durationMs = mediaPlayerManager?.durationMs?.collectAsState()?.value ?: 0L

    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ITunesTopBarStart, ITunesTopBarEnd)
                )
            )
            .border(width = 1.dp, color = ITunesTopBarBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Upper Control Strip: Transport Buttons | Retro LCD Screen | Search Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Transport Controls (Prev, Play, Next, Volume)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { mediaPlayerManager?.playPrevious() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous",
                        tint = Color(0xFF4A4E57),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { mediaPlayerManager?.togglePlayPause() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFD8DCE3))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color(0xFFA1A6B0), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF2C313A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { mediaPlayerManager?.playNext() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next",
                        tint = Color(0xFF4A4E57),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(18.dp)
                )
            }

            // Retro Glossy LCD Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ITunesLcdBackground)
                    .border(1.dp, ITunesLcdBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        if (currentMedia?.isVideo == true) {
                            mediaPlayerManager.openVideoPlayer()
                        } else if (currentMedia != null) {
                            mediaPlayerManager.showMusicPlayer()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentMedia?.title ?: "iTunes 11 Media Center",
                        style = TextStyle(
                            color = ITunesLcdTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentMedia?.let { media ->
                            if (media.albumOrCategory.isNotBlank()) "${media.subtitle} — ${media.albumOrCategory}"
                            else media.subtitle
                        } ?: "Select a tab below to browse library",
                        style = TextStyle(
                            color = ITunesLcdTextSecondary,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ITunesLcdProgressTrack)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(3.dp)
                                .background(ITunesLcdProgressFill)
                        )
                    }
                }
            }

            // Search Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8BCC6)),
                modifier = Modifier
                    .width(160.dp)
                    .height(30.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF8E95A2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF1C1D21)
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search",
                                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF9EA4B0))
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ITunesTopBarPreview() {
    ATunesLegacyTheme {
        ITunesTopBar(
            currentTab = MainTab.MUSIC,
            onTabSelected = {},
            searchQuery = "",
            onSearchQueryChanged = {}
        )
    }
}

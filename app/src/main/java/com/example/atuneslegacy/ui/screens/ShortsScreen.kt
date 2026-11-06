package com.example.atuneslegacy.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.importing.MediaImportHelper
import com.example.atuneslegacy.data.local.entity.ShortVideoEntity
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ShortsSubTab(val title: String) {
    SAVED("Saved"),
    FOR_YOU("For You")
}

@UnstableApi
@Composable
fun ShortsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                MediaImportHelper.scanAndImportATunesDirectory(context, viewModel.mediaRepository)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var currentSubTab by remember { mutableStateOf(ShortsSubTab.FOR_YOU) }
    val allShorts by viewModel.allShorts.collectAsState()
    val savedShorts by viewModel.savedShorts.collectAsState()

    // Stable For You Queue & Active Page Position
    var forYouQueue by remember { mutableStateOf<List<ShortVideoEntity>>(emptyList()) }
    var forYouCurrentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(allShorts) {
        if (allShorts.isNotEmpty()) {
            if (forYouQueue.isEmpty()) {
                forYouQueue = allShorts.shuffled()
            } else {
                val map = allShorts.associateBy { it.id }
                forYouQueue = forYouQueue.map { existing -> map[existing.id] ?: existing }
            }
        }
    }

    var activeSavedVideo by remember { mutableStateOf<ShortVideoEntity?>(null) }

    // Sequential background thumbnail loader for Saved tab (loads one at a time)
    var savedThumbnails by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    LaunchedEffect(currentSubTab, savedShorts) {
        if (currentSubTab == ShortsSubTab.SAVED) {
            for (short in savedShorts) {
                if (!savedThumbnails.containsKey(short.id)) {
                    val thumbUri = withContext(Dispatchers.IO) {
                        MediaImportHelper.extractFirstVideoThumbnail(context, Uri.parse(short.uri))
                    }
                    if (thumbUri.isNotBlank()) {
                        savedThumbnails = savedThumbnails + (short.id to thumbUri)
                    }
                }
            }
        }
    }

    // Intercept back button when watching a saved video
    if (currentSubTab == ShortsSubTab.SAVED && activeSavedVideo != null) {
        BackHandler {
            activeSavedVideo = null
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0F1115))) {
        // Top Subtab Switcher (Saved | For You)
        Surface(
            color = Color(0xFF1E222A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                ITunesSegmentedControl(
                    items = ShortsSubTab.entries,
                    selectedItem = currentSubTab,
                    onItemSelected = { newSubTab ->
                        if (newSubTab != currentSubTab) {
                            activeSavedVideo = null
                            currentSubTab = newSubTab
                        }
                    },
                    getItemLabel = { it.title }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentSubTab) {
                ShortsSubTab.FOR_YOU -> {
                    if (forYouQueue.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Shorts Available\n(Place videos in aTunes Library/Shorts/)", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        ShortsPlayerView(
                            queue = forYouQueue,
                            initialIndex = forYouCurrentIndex,
                            onIndexChange = { forYouCurrentIndex = it },
                            viewModel = viewModel,
                            context = context
                        )
                    }
                }
                ShortsSubTab.SAVED -> {
                    if (activeSavedVideo != null) {
                        // Play Saved Video full-screen
                        val savedIndex = savedShorts.indexOfFirst { it.id == activeSavedVideo?.id }
                        ShortsPlayerView(
                            queue = savedShorts,
                            initialIndex = if (savedIndex != -1) savedIndex else 0,
                            viewModel = viewModel,
                            context = context
                        )
                    } else {
                        // Saved Gallery View
                        if (savedShorts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Saved Shorts\n(Tap the floppy disk icon on any video to save)", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 120.dp),
                                contentPadding = PaddingValues(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(savedShorts) { index, short ->
                                    val thumbUri = savedThumbnails[short.id] ?: ""
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222A)),
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .fillMaxWidth()
                                            .clickable { activeSavedVideo = short }
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(0.7f)
                                                    .background(Color(0xFF16191E)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (thumbUri.isNotBlank()) {
                                                    AsyncImage(
                                                        model = thumbUri,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.White.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(42.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@UnstableApi
@Composable
private fun ShortsPlayerView(
    queue: List<ShortVideoEntity>,
    initialIndex: Int = 0,
    onIndexChange: (Int) -> Unit = {},
    viewModel: MainViewModel,
    context: Context
) {
    var currentIndex by remember(queue) { mutableIntStateOf(initialIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0))) }
    val currentShort = queue.getOrNull(currentIndex)

    LaunchedEffect(currentIndex) {
        onIndexChange(currentIndex)
    }

    var isPlaying by remember { mutableStateOf(true) }

    // Memory efficient ExoPlayer instance (loads ONLY 1 video at a time)
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    val isImageShort = remember(currentShort) {
        val uriStr = currentShort?.uri?.lowercase() ?: ""
        uriStr.endsWith(".jpg") || uriStr.endsWith(".jpeg") || uriStr.endsWith(".png") || uriStr.endsWith(".webp")
    }

    // Load active short video into player ONLY when video ID or URI changes!
    LaunchedEffect(currentShort?.id, currentShort?.uri, isImageShort) {
        if (currentShort != null && !isImageShort) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = MediaItem.fromUri(Uri.parse(currentShort.uri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
            exoPlayer.play()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isImageShort) {
            AsyncImage(
                model = currentShort?.uri,
                contentDescription = currentShort?.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top-Right Floppy Disk Save Icon
        currentShort?.let { short ->
            var localIsSaved by remember(short.id) { mutableStateOf(short.isSaved) }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (localIsSaved) Color(0xFF1D6CE5) else Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            localIsSaved = !localIsSaved
                            viewModel.toggleSaveShort(short)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Short",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Bottom Classic Metallic Bar (Previous Video | Pause/Play | Next Video)
        Surface(
            color = Color(0xFF181B20).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFirst = currentIndex <= 0
                val isLast = currentIndex >= queue.size - 1

                // 1. Previous Video Button
                Button(
                    onClick = {
                        if (!isFirst) {
                            currentIndex--
                            isPlaying = true
                        }
                    },
                    enabled = !isFirst,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C3038),
                        disabledContainerColor = Color(0xFF181B20).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Previous",
                            tint = if (!isFirst) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Prev Video", fontSize = 10.sp, color = if (!isFirst) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Pause / Play Button
                Button(
                    onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3038)),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(if (isPlaying) "Pause" else "Play", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // 3. Next Video Button
                Button(
                    onClick = {
                        if (!isLast) {
                            currentIndex++
                            isPlaying = true
                        }
                    },
                    enabled = !isLast,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C3038),
                        disabledContainerColor = Color(0xFF181B20).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Next",
                            tint = if (!isLast) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Next Video", fontSize = 10.sp, color = if (!isLast) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

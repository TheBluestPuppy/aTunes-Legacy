package com.example.atuneslegacy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CoverFlowScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val musicSubTab by viewModel.musicSubTab.collectAsState()
    val songs by viewModel.allSongs.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()

    var selectedItemIndex by remember(currentTab, musicSubTab) { mutableStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var flippedItemIndex by remember<androidx.compose.runtime.MutableState<Int?>> { mutableStateOf(null) }

    // Determine items based on current tab
    data class CoverItem(val id: String, val title: String, val subtitle: String, val artworkUri: String, val songs: List<SongEntity>)

    val items = remember(currentTab, musicSubTab, songs, albums, playlists) {
        when (currentTab) {
            MainTab.MUSIC -> {
                when (musicSubTab) {
                    MusicSubTab.SONGS, MusicSubTab.ALBUMS, MusicSubTab.GENRES -> {
                        albums.map { album ->
                            val albSongs = songs.filter { it.albumTitle.equals(album.title, ignoreCase = true) }
                            CoverItem(
                                id = "album_${album.id}",
                                title = album.title,
                                subtitle = album.albumArtist,
                                artworkUri = album.albumCoverUri,
                                songs = albSongs.ifEmpty { songs }
                            )
                        }
                    }
                    MusicSubTab.ARTISTS -> {
                        val artists = songs.map { it.artist }.distinct().sorted()
                        artists.map { artist ->
                            val artSongs = songs.filter { it.artist.equals(artist, ignoreCase = true) }
                            val firstAlbum = artSongs.minByOrNull { it.year }
                            CoverItem(
                                id = "artist_$artist",
                                title = artist,
                                subtitle = "${artSongs.size} Songs",
                                artworkUri = firstAlbum?.artworkUri ?: "",
                                songs = artSongs.sortedBy { it.title }
                            )
                        }
                    }
                    MusicSubTab.PLAYLISTS -> {
                        playlists.map { pl ->
                            CoverItem(
                                id = "playlist_${pl.id}",
                                title = pl.title,
                                subtitle = "Playlist",
                                artworkUri = "",
                                songs = songs
                            )
                        }
                    }
                    MusicSubTab.MUSIC_VIDEOS -> {
                        songs.filter { it.isMusicVideo }.map { video ->
                            CoverItem(
                                id = "video_${video.id}",
                                title = video.title,
                                subtitle = video.artist,
                                artworkUri = video.artworkUri,
                                songs = listOf(video)
                            )
                        }
                    }
                }
            }
            else -> {
                albums.map { album ->
                    CoverItem(
                        id = "album_${album.id}",
                        title = album.title,
                        subtitle = album.albumArtist,
                        artworkUri = album.albumCoverUri,
                        songs = songs
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(items) {
                if (items.isNotEmpty()) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val targetIndex = (selectedItemIndex - (dragOffset / 120f).roundToInt()).coerceIn(0, items.size - 1)
                            selectedItemIndex = targetIndex
                            dragOffset = 0f
                            flippedItemIndex = null
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) {
            Text("No Media for Cover Flow", color = Color.Gray, fontSize = 16.sp)
        } else {
            val activeIndex = (selectedItemIndex - (dragOffset / 120f).roundToInt()).coerceIn(0, items.size - 1)

            // Render 3D Perspective Cover Flow Row
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-60).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val distance = index - activeIndex
                        val isCenter = distance == 0
                        val rotationY = (distance * -25f).coerceIn(-60f, 60f)
                        val scale = if (isCenter) 1.1f else 0.8f
                        val alpha = if (abs(distance) > 3) 0f else 1f

                        val isFlipped = flippedItemIndex == index

                        val animatedRotation by animateFloatAsState(targetValue = if (isFlipped) 180f else rotationY, label = "rotation")

                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .graphicsLayer {
                                    this.rotationY = animatedRotation
                                    cameraDistance = 12f * density
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .clickable {
                                    if (isCenter) {
                                        if (flippedItemIndex == index) {
                                            flippedItemIndex = null
                                        } else {
                                            flippedItemIndex = index
                                        }
                                    } else {
                                        selectedItemIndex = index
                                        flippedItemIndex = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (animatedRotation > 90f && animatedRotation < 270f) {
                                // Back of Cover: Tracklist
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C1C1E))
                                        .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                        .graphicsLayer { this.rotationY = 180f }
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                            IconButton(onClick = { flippedItemIndex = null }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = "Flip", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            itemsIndexed(item.songs) { sIdx, song ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.playSong(song, item.songs.drop(sIdx))
                                                        }
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${sIdx + 1}. ${song.title}",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = formatDuration(song.duration),
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Front of Cover: Artwork & Title
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2C2C2E))
                                        .border(1.dp, Color(0xFF48484A), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.artworkUri.isNotBlank()) {
                                        AsyncImage(
                                            model = item.artworkUri,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .padding(8.dp)
                                    ) {
                                        Text(text = item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = item.subtitle, fontSize = 10.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

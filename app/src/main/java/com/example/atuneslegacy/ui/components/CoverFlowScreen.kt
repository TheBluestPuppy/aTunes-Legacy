package com.example.atuneslegacy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

data class CoverFlowModelItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkUri: String,
    val isVertical: Boolean = false,
    val songsOrEpisodes: List<Any> = emptyList(),
    val action: (() -> Unit)? = null
)

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
    val tvShows by viewModel.allTvShows.collectAsState()
    val movies by viewModel.allMovies.collectAsState()
    val tvEpisodes by viewModel.mediaRepository.getAllTvEpisodes().collectAsState(initial = emptyList())
    val podcasts by viewModel.allPodcastShows.collectAsState()
    val podcastEpisodes by viewModel.mediaRepository.getAllPodcastEpisodes().collectAsState(initial = emptyList())
    val audiobooks by viewModel.allAudiobooks.collectAsState()

    val scope = rememberCoroutineScope()
    var flippedItemIndex by remember { mutableStateOf<Int?>(null) }

    val items = remember(currentTab, musicSubTab, songs, albums, playlists, tvShows, movies, tvEpisodes, podcasts, podcastEpisodes, audiobooks) {
        when (currentTab) {
            MainTab.MUSIC -> {
                when (musicSubTab) {
                    MusicSubTab.SONGS, MusicSubTab.ALBUMS, MusicSubTab.GENRES -> {
                        albums.map { album ->
                            val albSongs = songs.filter { it.albumTitle.equals(album.title, ignoreCase = true) }
                            CoverFlowModelItem(
                                id = "album_${album.id}",
                                title = album.title,
                                subtitle = album.albumArtist,
                                artworkUri = album.albumCoverUri,
                                isVertical = false,
                                songsOrEpisodes = albSongs.ifEmpty { songs }
                            )
                        }
                    }
                    MusicSubTab.ARTISTS -> {
                        val artists = songs.map { it.artist }.distinct().sorted()
                        artists.map { artist ->
                            val artSongs = songs.filter { it.artist.equals(artist, ignoreCase = true) }
                            val firstAlbum = artSongs.minByOrNull { it.year }
                            CoverFlowModelItem(
                                id = "artist_$artist",
                                title = artist,
                                subtitle = "${artSongs.size} Songs",
                                artworkUri = firstAlbum?.artworkUri ?: "",
                                isVertical = false,
                                songsOrEpisodes = artSongs.sortedBy { it.title }
                            )
                        }
                    }
                    MusicSubTab.PLAYLISTS -> {
                        playlists.map { pl ->
                            CoverFlowModelItem(
                                id = "playlist_${pl.id}",
                                title = pl.title,
                                subtitle = "Playlist",
                                artworkUri = "",
                                isVertical = false,
                                songsOrEpisodes = songs
                            )
                        }
                    }
                    MusicSubTab.MUSIC_VIDEOS -> {
                        songs.filter { it.isMusicVideo }.map { video ->
                            CoverFlowModelItem(
                                id = "video_${video.id}",
                                title = video.title,
                                subtitle = video.artist,
                                artworkUri = video.artworkUri,
                                isVertical = true,
                                songsOrEpisodes = listOf(video)
                            )
                        }
                    }
                }
            }
            MainTab.TV_SHOWS -> {
                tvShows.map { show ->
                    val showEps = tvEpisodes.filter { it.showTitle.equals(show.title, ignoreCase = true) }
                    CoverFlowModelItem(
                        id = "tv_${show.id}",
                        title = show.title,
                        subtitle = "${showEps.size} Episodes • ${show.releaseYear}",
                        artworkUri = show.logoThumbnailUri,
                        isVertical = true,
                        songsOrEpisodes = showEps
                    )
                }
            }
            MainTab.MOVIES -> {
                movies.map { movie ->
                    CoverFlowModelItem(
                        id = "movie_${movie.id}",
                        title = movie.title,
                        subtitle = "${movie.releaseYear} • ${movie.parentalRating}",
                        artworkUri = movie.thumbnailUri,
                        isVertical = true,
                        songsOrEpisodes = listOf(movie),
                        action = { viewModel.playMovie(movie) }
                    )
                }
            }
            MainTab.PODCASTS -> {
                podcasts.map { show ->
                    CoverFlowModelItem(
                        id = "podcast_${show.id}",
                        title = show.title,
                        subtitle = show.creatorHost,
                        artworkUri = show.logoThumbnailUri,
                        isVertical = true,
                        songsOrEpisodes = podcastEpisodes.filter { it.podcastName.equals(show.title, ignoreCase = true) }
                    )
                }
            }
            MainTab.AUDIOBOOKS -> {
                audiobooks.map { book ->
                    CoverFlowModelItem(
                        id = "book_${book.id}",
                        title = book.title,
                        subtitle = book.author,
                        artworkUri = book.portraitCoverUri,
                        isVertical = true,
                        songsOrEpisodes = emptyList(),
                        action = { viewModel.playAudiobook(book) }
                    )
                }
            }
            else -> emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) {
            Text("No Media Available for Cover Flow", color = Color.Gray, fontSize = 16.sp)
        } else {
            val pagerState = rememberPagerState(pageCount = { items.size })
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp.dp

            val isVertical = items.firstOrNull()?.isVertical == true
            val cardWidth = if (isVertical) 150.dp else 200.dp
            val cardHeight = if (isVertical) 220.dp else 200.dp
            val sidePadding = ((screenWidthDp - cardWidth) / 2).coerceAtLeast(16.dp)

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = sidePadding),
                pageSpacing = (-60).dp,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val item = items[pageIndex]
                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                val absOffset = abs(pageOffset)

                // 3D Perspective Cover Flow Calculations
                val rotationY = when {
                    pageOffset > 0f -> (60f * pageOffset).coerceAtMost(60f)
                    pageOffset < 0f -> (-60f * -pageOffset).coerceAtLeast(-60f)
                    else -> 0f
                }
                val scale = (1.2f - absOffset * 0.35f).coerceIn(0.8f, 1.2f)
                val alpha = (1f - absOffset * 0.25f).coerceIn(0.2f, 1f)
                val itemZIndex = 100f - absOffset

                val isFlipped = flippedItemIndex == pageIndex
                val animatedRotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else rotationY,
                    animationSpec = tween(durationMillis = 400),
                    label = "rotation"
                )

                Box(
                    modifier = Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .zIndex(itemZIndex)
                        .graphicsLayer {
                            this.rotationY = animatedRotation
                            cameraDistance = 14f * density
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clickable {
                            if (absOffset < 0.2f) {
                                if (item.action != null) {
                                    item.action.invoke()
                                } else if (item.songsOrEpisodes.isNotEmpty()) {
                                    flippedItemIndex = if (flippedItemIndex == pageIndex) null else pageIndex
                                }
                            } else {
                                scope.launch {
                                    flippedItemIndex = null
                                    pagerState.animateScrollToPage(pageIndex)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (animatedRotation > 90f && animatedRotation < 270f) {
                        // Back of Cover: Tracklist / Details
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
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { flippedItemIndex = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Flip",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    itemsIndexed(item.songsOrEpisodes) { sIdx, entry ->
                                        when (entry) {
                                            is SongEntity -> {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.playSong(
                                                                entry,
                                                                item.songsOrEpisodes.filterIsInstance<SongEntity>().drop(sIdx)
                                                            )
                                                        }
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${sIdx + 1}. ${entry.title}",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            is TvEpisodeEntity -> {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { viewModel.playTvEpisode(entry) }
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "S${entry.seasonNumber}E${entry.episodeNumber}: ${entry.title}",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            is MovieEntity -> {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(text = entry.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text(text = "Cast: ${entry.artists}", fontSize = 10.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(onClick = { viewModel.playMovie(entry) }) {
                                                        Text("Play Movie")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Front of Cover
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
                                    imageVector = if (item.isVertical) Icons.Default.Tv else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

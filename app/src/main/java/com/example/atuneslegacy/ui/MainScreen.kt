package com.example.atuneslegacy.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.example.atuneslegacy.ui.components.CoverFlowScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.atuneslegacy.player.MediaType
import com.example.atuneslegacy.ui.components.AddMediaDialog
import com.example.atuneslegacy.ui.components.AlbumMetadataDialog
import com.example.atuneslegacy.ui.components.AudiobookMetadataDialog
import com.example.atuneslegacy.ui.components.BookSeriesMetadataDialog
import com.example.atuneslegacy.ui.components.CreatePlaylistDialog
import com.example.atuneslegacy.ui.components.ITunesTopBar
import com.example.atuneslegacy.ui.components.MovieMetadataDialog
import com.example.atuneslegacy.ui.components.PodcastEpisodeMetadataDialog
import com.example.atuneslegacy.ui.components.PodcastShowMetadataDialog
import com.example.atuneslegacy.ui.components.SongMetadataDialog
import com.example.atuneslegacy.ui.components.TvEpisodeMetadataDialog
import com.example.atuneslegacy.ui.components.TvShowMetadataDialog
import com.example.atuneslegacy.ui.components.VideoPlayerOverlay
import com.example.atuneslegacy.ui.components.MusicPlayerOverlay
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.screens.AudiobooksScreen
import com.example.atuneslegacy.ui.screens.GamesScreen
import com.example.atuneslegacy.ui.screens.MoviesScreen
import com.example.atuneslegacy.ui.screens.MusicScreen
import com.example.atuneslegacy.ui.screens.PhotosScreen
import com.example.atuneslegacy.ui.screens.PodcastsScreen
import com.example.atuneslegacy.ui.screens.ProfileScreen
import com.example.atuneslegacy.ui.screens.RecordingsScreen
import com.example.atuneslegacy.ui.screens.SettingsScreen
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.screens.TvShowsScreen
import com.example.atuneslegacy.ui.theme.ITunesActiveSegmentEnd
import com.example.atuneslegacy.ui.theme.ITunesActiveSegmentStart
import com.example.atuneslegacy.ui.theme.ITunesTopBarBorder
import com.example.atuneslegacy.ui.theme.ITunesTopBarEnd
import com.example.atuneslegacy.ui.theme.ITunesTopBarStart
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        CoverFlowScreen(viewModel = viewModel, modifier = modifier)
        return
    }

    val themeColors = LocalThemeColors.current
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val songs by viewModel.allSongs.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val tvShows by viewModel.allTvShows.collectAsState()
    val movies by viewModel.allMovies.collectAsState()
    val podcasts by viewModel.allPodcastShows.collectAsState()
    val audiobooks by viewModel.allAudiobooks.collectAsState()

    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val showAddMediaDialog by viewModel.showAddMediaDialog.collectAsState()
    val showVideoPlayer by viewModel.mediaPlayerManager.showVideoPlayer.collectAsState()
    val showMusicPlayer by viewModel.mediaPlayerManager.showMusicPlayer.collectAsState()
    val showProfilePage by viewModel.showProfilePage.collectAsState()

    // Screen Off Listener for Video Playback Control
    val context = LocalContext.current
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    val currentMedia = viewModel.mediaPlayerManager.currentMedia.value
                    if (currentMedia != null && currentMedia.isVideo) {
                        if (currentMedia.mediaType != MediaType.MUSIC_VIDEO) {
                            viewModel.mediaPlayerManager.exoPlayer.pause()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Editing Dialog States
    val editingSong by viewModel.editingSong.collectAsState()
    val editingAlbum by viewModel.editingAlbum.collectAsState()
    val editingTvShow by viewModel.editingTvShow.collectAsState()
    val editingTvEpisode by viewModel.editingTvEpisode.collectAsState()
    val editingMovie by viewModel.editingMovie.collectAsState()
    val editingPodcastShow by viewModel.editingPodcastShow.collectAsState()
    val editingPodcastEpisode by viewModel.editingPodcastEpisode.collectAsState()
    val editingAudiobook by viewModel.editingAudiobook.collectAsState()
    val editingBookSeries by viewModel.editingBookSeries.collectAsState()

    Scaffold(
        topBar = {
            ITunesTopBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                searchQuery = searchQuery,
                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                mediaPlayerManager = viewModel.mediaPlayerManager
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(themeColors.topBarStart, themeColors.topBarEnd)
                        )
                    )
                    .border(width = 0.5.dp, color = themeColors.topBarBorder)
            ) {
                // Status Bar Info Row
                val statusText = when (currentTab) {
                    MainTab.MUSIC -> "${songs.size} Songs, ${albums.size} Albums"
                    MainTab.TV_SHOWS -> "${tvShows.size} TV Shows"
                    MainTab.MOVIES -> "${movies.size} Movies"
                    MainTab.PODCASTS -> "${podcasts.size} Podcasts"
                    MainTab.AUDIOBOOKS -> "${audiobooks.size} Audiobooks"
                    MainTab.PHOTOS -> "Photos Library"
                    MainTab.RECORDINGS -> "Voice Recordings"
                    MainTab.GAMES -> "Apps & Games"
                    MainTab.SETTINGS -> "aTunes Settings & Profile"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.textSecondary
                    )
                }

                HorizontalDivider(color = themeColors.topBarBorder, thickness = 0.5.dp)

                // Scrollable Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        val shape = RoundedCornerShape(6.dp)

                        Column(
                            modifier = Modifier
                                .width(74.dp)
                                .height(42.dp)
                                .clip(shape)
                                .background(
                                    brush = if (isSelected) {
                                        Brush.verticalGradient(
                                            listOf(themeColors.activeSegmentStart, themeColors.activeSegmentEnd)
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(themeColors.surface, themeColors.topBarEnd)
                                        )
                                    }
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSelected) themeColors.primaryAccent else themeColors.topBarBorder,
                                    shape = shape
                                )
                                .clickable { viewModel.selectTab(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color.White else themeColors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.White else themeColors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showProfilePage) {
                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { viewModel.setShowProfilePage(false) }
                )
            } else {
                when (currentTab) {
                    MainTab.MUSIC -> MusicScreen(viewModel = viewModel)
                    MainTab.TV_SHOWS -> TvShowsScreen(viewModel = viewModel)
                    MainTab.MOVIES -> MoviesScreen(viewModel = viewModel)
                    MainTab.PODCASTS -> PodcastsScreen(viewModel = viewModel)
                    MainTab.AUDIOBOOKS -> AudiobooksScreen(viewModel = viewModel)
                    MainTab.PHOTOS -> PhotosScreen(viewModel = viewModel)
                    MainTab.RECORDINGS -> RecordingsScreen(viewModel = viewModel)
                    MainTab.GAMES -> GamesScreen(viewModel = viewModel)
                    MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.setShowCreatePlaylistDialog(false) },
            onCreate = { title -> viewModel.createPlaylist(title) }
        )
    }

    if (showAddMediaDialog) {
        AddMediaDialog(
            onDismiss = { viewModel.setShowAddMediaDialog(false) },
            onImportFiles = { uris, category -> viewModel.importFiles(uris, category) }
        )
    }

    // Metadata Editing Dialogs
    editingSong?.let { song ->
        SongMetadataDialog(
            song = song,
            onDismiss = { viewModel.setEditingSong(null) },
            onSave = { updated -> viewModel.updateSong(updated) },
            onDelete = { viewModel.deleteSong(song) }
        )
    }

    editingAlbum?.let { album ->
        AlbumMetadataDialog(
            album = album,
            onDismiss = { viewModel.setEditingAlbum(null) },
            onSave = { oldTitle, updated -> viewModel.updateAlbum(oldTitle, updated) },
            onDelete = { viewModel.deleteAlbum(album) }
        )
    }

    editingTvShow?.let { show ->
        TvShowMetadataDialog(
            show = show,
            onDismiss = { viewModel.setEditingTvShow(null) },
            onSave = { oldTitle, updated -> viewModel.updateTvShow(oldTitle, updated) },
            onDelete = { viewModel.deleteTvShow(show) }
        )
    }

    editingTvEpisode?.let { episode ->
        TvEpisodeMetadataDialog(
            episode = episode,
            onDismiss = { viewModel.setEditingTvEpisode(null) },
            onSave = { updated -> viewModel.updateTvEpisode(updated) },
            onDelete = { viewModel.deleteTvEpisode(episode) }
        )
    }

    editingMovie?.let { movie ->
        MovieMetadataDialog(
            movie = movie,
            onDismiss = { viewModel.setEditingMovie(null) },
            onSave = { updated -> viewModel.updateMovie(updated) },
            onDelete = { viewModel.deleteMovie(movie) }
        )
    }

    editingPodcastShow?.let { show ->
        PodcastShowMetadataDialog(
            show = show,
            onDismiss = { viewModel.setEditingPodcastShow(null) },
            onSave = { oldName, updated -> viewModel.updatePodcastShow(oldName, updated) },
            onDelete = { viewModel.deletePodcastShow(show) }
        )
    }

    editingPodcastEpisode?.let { episode ->
        PodcastEpisodeMetadataDialog(
            episode = episode,
            onDismiss = { viewModel.setEditingPodcastEpisode(null) },
            onSave = { updated -> viewModel.updatePodcastEpisode(updated) },
            onDelete = { viewModel.deletePodcastEpisode(episode) }
        )
    }

    editingAudiobook?.let { book ->
        AudiobookMetadataDialog(
            book = book,
            onDismiss = { viewModel.setEditingAudiobook(null) },
            onSave = { updated -> viewModel.updateAudiobook(updated) },
            onDelete = { viewModel.deleteAudiobook(book) }
        )
    }

    editingBookSeries?.let { series ->
        BookSeriesMetadataDialog(
            series = series,
            onDismiss = { viewModel.setEditingBookSeries(null) },
            onSave = { oldTitle, updated -> viewModel.updateBookSeries(oldTitle, updated) },
            onDelete = { viewModel.deleteBookSeries(series) }
        )
    }

    // Video Player Fullscreen Overlay
    if (showVideoPlayer) {
        VideoPlayerOverlay(
            mediaPlayerManager = viewModel.mediaPlayerManager
        )
    }

    // Music Player Fullscreen Overlay
    if (showMusicPlayer) {
        MusicPlayerOverlay(
            mediaPlayerManager = viewModel.mediaPlayerManager,
            onDismiss = { viewModel.mediaPlayerManager.hideMusicPlayer() }
        )
    }
}

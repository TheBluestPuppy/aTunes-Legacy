package com.example.atuneslegacy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.ui.components.AlbumGridCard
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.components.SongTableHeader
import com.example.atuneslegacy.ui.components.SongTableRow
import com.example.atuneslegacy.ui.components.formatDuration
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun MusicScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val currentSubTab by viewModel.musicSubTab.collectAsState()
    val songs by viewModel.musicSongs.collectAsState()
    val allSongsList by viewModel.allSongs.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val musicVideos by viewModel.musicVideos.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    val totalSongsDuration = songs.sumOf { it.duration }
    val formattedTotalTime = formatDuration(totalSongsDuration)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Sub-Navigation Segmented Switcher
        ITunesSegmentedControl(
            items = MusicSubTab.entries,
            selectedItem = currentSubTab,
            onItemSelected = { viewModel.selectMusicSubTab(it) },
            getItemLabel = { it.title }
        )

        // Main Subtab Content Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentSubTab) {
                MusicSubTab.SONGS -> SongsSubScreen(songs = songs, viewModel = viewModel)
                MusicSubTab.ALBUMS -> AlbumsSubScreen(
                    albums = albums,
                    songs = songs,
                    selectedAlbum = selectedAlbum,
                    onSelectAlbum = { viewModel.selectAlbum(it) },
                    viewModel = viewModel
                )
                MusicSubTab.ARTISTS -> ArtistsSubScreen(
                    songs = songs,
                    selectedArtist = selectedArtist,
                    onSelectArtist = { viewModel.selectArtist(it) },
                    viewModel = viewModel
                )
                MusicSubTab.GENRES -> GenresSubScreen(
                    songs = songs,
                    selectedGenre = selectedGenre,
                    onSelectGenre = { viewModel.selectGenre(it) },
                    viewModel = viewModel
                )
                MusicSubTab.PLAYLISTS -> PlaylistsSubScreen(
                    playlists = playlists,
                    allSongs = allSongsList,
                    selectedPlaylist = selectedPlaylist,
                    onSelectPlaylist = { viewModel.selectPlaylist(it) },
                    onCreatePlaylistClick = { viewModel.setShowCreatePlaylistDialog(true) },
                    viewModel = viewModel
                )
                MusicSubTab.MUSIC_VIDEOS -> MusicVideosSubScreen(videos = musicVideos, viewModel = viewModel)
            }
        }

        // Authentic iTunes Summary Status Footer
        val summaryText = when (currentSubTab) {
            MusicSubTab.SONGS -> "${songs.size} Songs, $formattedTotalTime"
            MusicSubTab.ALBUMS -> "${albums.size} Albums, ${songs.size} Songs"
            MusicSubTab.ARTISTS -> "${songs.map { it.artist }.distinct().size} Artists"
            MusicSubTab.GENRES -> "${songs.map { it.genre }.filter { it.isNotBlank() }.distinct().size} Genres"
            MusicSubTab.PLAYLISTS -> "${playlists.size} Playlists"
            MusicSubTab.MUSIC_VIDEOS -> "${musicVideos.size} Music Videos"
        }

        Surface(
            color = themeColors.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SongsSubScreen(songs: List<SongEntity>, viewModel: MainViewModel) {
    if (songs.isEmpty()) {
        EmptyMediaNotice("No Songs Found", "Import audio files to your iTunes library.")
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(songs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        onClick = { viewModel.playSong(song, songs) },
                        onLongClick = { viewModel.setEditingSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsSubScreen(
    albums: List<AlbumEntity>,
    songs: List<SongEntity>,
    selectedAlbum: AlbumEntity?,
    onSelectAlbum: (AlbumEntity?) -> Unit,
    viewModel: MainViewModel
) {
    val themeColors = LocalThemeColors.current

    if (selectedAlbum != null) {
        val albumSongs = songs.filter { it.albumTitle.equals(selectedAlbum.title, ignoreCase = true) }
        val albumTime = formatDuration(albumSongs.sumOf { it.duration })

        Column(modifier = Modifier.fillMaxSize()) {
            // Album Detail Header Banner
            Surface(
                color = themeColors.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onSelectAlbum(null) }) {
                        Text("← All Albums", fontSize = 11.sp, color = themeColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(themeColors.background)
                            .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAlbum.albumCoverUri.isNotBlank()) {
                            AsyncImage(
                                model = selectedAlbum.albumCoverUri,
                                contentDescription = selectedAlbum.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(onClick = {}, onLongClick = { viewModel.setEditingAlbum(selectedAlbum) })
                    ) {
                        Text(
                            text = selectedAlbum.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${selectedAlbum.albumArtist} • ${selectedAlbum.releaseYear} • ${selectedAlbum.genre}",
                            fontSize = 12.sp,
                            color = themeColors.textSecondary
                        )
                        Text(
                            text = "${albumSongs.size} Songs, $albumTime",
                            fontSize = 11.sp,
                            color = themeColors.textSecondary
                        )
                    }
                }
            }

            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(albumSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        onClick = { viewModel.playSong(song, albumSongs) },
                        onLongClick = { viewModel.setEditingSong(song) }
                    )
                }
            }
        }
    } else if (albums.isEmpty()) {
        EmptyMediaNotice("No Albums Found", "No album records present in library.")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums) { album ->
                AlbumGridCard(
                    album = album,
                    onClick = { onSelectAlbum(album) },
                    onLongClick = { viewModel.setEditingAlbum(album) }
                )
            }
        }
    }
}

@Composable
private fun ArtistsSubScreen(
    songs: List<SongEntity>,
    selectedArtist: String?,
    onSelectArtist: (String?) -> Unit,
    viewModel: MainViewModel
) {
    val themeColors = LocalThemeColors.current
    val artists = songs.map { it.artist }.distinct().sorted()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Artist Sidebar
        LazyColumn(
            modifier = Modifier
                .width(160.dp)
                .fillMaxSize()
                .background(themeColors.surface)
                .border(width = 0.5.dp, color = themeColors.surfaceBorder)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectArtist(null) }
                        .background(if (selectedArtist == null) themeColors.primaryAccent else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "All Artists (${artists.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedArtist == null) Color.White else themeColors.textPrimary
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder)
            }
            items(artists) { artist ->
                val isSelected = artist.equals(selectedArtist, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectArtist(artist) }
                        .background(if (isSelected) themeColors.primaryAccent else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = artist,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else themeColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder)
            }
        }

        // Right Content Column
        val filteredSongs = if (selectedArtist == null) songs else songs.filter { it.artist.equals(selectedArtist, ignoreCase = true) }
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(filteredSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        onClick = { viewModel.playSong(song, filteredSongs) },
                        onLongClick = { viewModel.setEditingSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GenresSubScreen(
    songs: List<SongEntity>,
    selectedGenre: String?,
    onSelectGenre: (String?) -> Unit,
    viewModel: MainViewModel
) {
    val themeColors = LocalThemeColors.current
    val genres = songs.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()

    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .width(160.dp)
                .fillMaxSize()
                .background(themeColors.surface)
                .border(width = 0.5.dp, color = themeColors.surfaceBorder)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(null) }
                        .background(if (selectedGenre == null) themeColors.primaryAccent else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "All Genres (${genres.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedGenre == null) Color.White else themeColors.textPrimary
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder)
            }
            items(genres) { genre ->
                val isSelected = genre.equals(selectedGenre, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(genre) }
                        .background(if (isSelected) themeColors.primaryAccent else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = genre,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else themeColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder)
            }
        }

        val filteredSongs = if (selectedGenre == null) songs else songs.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(filteredSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        onClick = { viewModel.playSong(song, filteredSongs) },
                        onLongClick = { viewModel.setEditingSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsSubScreen(
    playlists: List<PlaylistEntity>,
    allSongs: List<SongEntity>,
    selectedPlaylist: PlaylistEntity?,
    onSelectPlaylist: (PlaylistEntity?) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    viewModel: MainViewModel
) {
    val themeColors = LocalThemeColors.current

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Playlists Sidebar
        Column(
            modifier = Modifier
                .width(180.dp)
                .fillMaxSize()
                .background(themeColors.surface)
                .border(width = 0.5.dp, color = themeColors.surfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PLAYLISTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.textSecondary)
                Button(
                    onClick = onCreatePlaylistClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("New", fontSize = 10.sp)
                }
            }
            HorizontalDivider(color = themeColors.surfaceBorder)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(playlists) { playlist ->
                    val isSelected = playlist == selectedPlaylist
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPlaylist(playlist) }
                            .background(if (isSelected) themeColors.primaryAccent else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else themeColors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = playlist.title,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else themeColors.textPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(color = themeColors.surfaceBorder)
                }
            }
        }

        // Right Playlist Content
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            if (selectedPlaylist != null) {
                Surface(
                    color = themeColors.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = selectedPlaylist.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                            Text(text = "Custom Playlist • ${allSongs.size} Songs", fontSize = 11.sp, color = themeColors.textSecondary)
                        }
                    }
                }
                SongTableHeader()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(allSongs) { index, song ->
                        SongTableRow(
                            song = song,
                            index = index,
                            onClick = { viewModel.playSong(song, allSongs) },
                            onLongClick = { viewModel.setEditingSong(song) }
                        )
                    }
                }
            } else {
                EmptyMediaNotice("Select a Playlist", "Choose or create a playlist from the left sidebar.")
            }
        }
    }
}

@Composable
private fun MusicVideosSubScreen(
    videos: List<SongEntity>,
    viewModel: MainViewModel
) {
    if (videos.isEmpty()) {
        EmptyMediaNotice(
            "No Music Videos",
            "No music videos found in your aTunes library."
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            SongTableHeader()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(videos) { index, video ->
                    SongTableRow(
                        song = video,
                        index = index,
                        showArtwork = true,
                        onClick = { viewModel.playSong(video, videos) },
                        onLongClick = { viewModel.setEditingSong(video) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMediaNotice(title: String, subtitle: String) {
    val themeColors = LocalThemeColors.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = themeColors.textSecondary)
        }
    }
}

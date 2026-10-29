package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.ui.components.AlbumGridCard
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.components.SongTableHeader
import com.example.atuneslegacy.ui.components.SongTableRow
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun MusicScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
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
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        // Sub-Navigation Segmented Switcher
        ITunesSegmentedControl(
            items = MusicSubTab.entries,
            selectedItem = currentSubTab,
            onItemSelected = { viewModel.selectMusicSubTab(it) },
            getItemLabel = { it.title }
        )

        // Content Area based on selected sub-tab
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
    }
}


@Composable
private fun SongsSubScreen(songs: List<SongEntity>, viewModel: MainViewModel) {
    val context = LocalContext.current

    if (songs.isEmpty()) {
        EmptyMediaNotice("No Songs Found", "Add audio files to your iTunes library.")
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
    if (selectedAlbum != null) {
        val albumSongs = songs.filter { it.albumTitle.equals(selectedAlbum.title, ignoreCase = true) }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E6EE))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { onSelectAlbum(null) }) {
                    Text("← Back to Albums", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { viewModel.setEditingAlbum(selectedAlbum) })
                ) {
                    Text(text = selectedAlbum.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${selectedAlbum.albumArtist} • ${selectedAlbum.releaseYear} • ${selectedAlbum.genre}", fontSize = 12.sp, color = Color(0xFF606674))
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
    val artists = songs.map { it.artist }.distinct().sorted()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Artist Sidebar
        LazyColumn(
            modifier = Modifier
                .width(160.dp)
                .fillMaxSize()
                .background(Color(0xFFE8EBF0))
                .border(width = 0.5.dp, color = Color(0xFFC0C5CE))
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectArtist(null) }
                        .background(if (selectedArtist == null) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "All Artists (${artists.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedArtist == null) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFD0D4DD))
            }
            items(artists) { artist ->
                val isSelected = artist.equals(selectedArtist, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectArtist(artist) }
                        .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = artist,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFE0E3EA))
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
    val genres = songs.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()

    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .width(160.dp)
                .fillMaxSize()
                .background(Color(0xFFE8EBF0))
                .border(width = 0.5.dp, color = Color(0xFFC0C5CE))
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(null) }
                        .background(if (selectedGenre == null) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "All Genres (${genres.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedGenre == null) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFD0D4DD))
            }
            items(genres) { genre ->
                val isSelected = genre.equals(selectedGenre, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(genre) }
                        .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = genre,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFE0E3EA))
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
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Playlists Sidebar
        Column(
            modifier = Modifier
                .width(180.dp)
                .fillMaxSize()
                .background(Color(0xFFE8EBF0))
                .border(width = 0.5.dp, color = Color(0xFFC0C5CE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PLAYLISTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF606674))
                Button(
                    onClick = onCreatePlaylistClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ITunesAccentBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("New", fontSize = 10.sp)
                }
            }
            HorizontalDivider(color = Color(0xFFD0D4DD))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(playlists) { playlist ->
                    val isSelected = playlist == selectedPlaylist
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPlaylist(playlist) }
                            .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF4A505C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = playlist.title,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Color(0xFF2C3038),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE0E3EA))
                }
            }
        }

        // Right Playlist Content
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            if (selectedPlaylist != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E6EE))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = selectedPlaylist.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Custom Playlist", fontSize = 11.sp, color = Color(0xFF606674))
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
    val context = LocalContext.current

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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A505C))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF888E9A))
        }
    }
}

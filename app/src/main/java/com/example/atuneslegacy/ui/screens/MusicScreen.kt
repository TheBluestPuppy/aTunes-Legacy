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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.ArtistEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.ui.components.AlbumGridCard
import com.example.atuneslegacy.ui.components.ArtistMetadataDialog
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
    val allArtists by viewModel.allArtists.collectAsState()
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
                    albums = albums,
                    allArtists = allArtists,
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
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

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
                        isMultiSelectMode = isMultiSelectMode,
                        isChecked = selectedSongIds.contains(song.id),
                        onCheckedChange = { viewModel.toggleSongSelection(song.id) },
                        onClick = {
                            if (isMultiSelectMode) viewModel.toggleSongSelection(song.id)
                            else viewModel.playSong(song, songs)
                        },
                        onLongClick = { viewModel.toggleSongSelection(song.id) }
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
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

    if (selectedAlbum != null) {
        val albumSongs = songs.filter { it.albumTitle.equals(selectedAlbum.title, ignoreCase = true) }
            .sortedBy { it.trackNumber }
        val albumTime = formatDuration(albumSongs.sumOf { it.duration })

        Column(modifier = Modifier.fillMaxSize()) {
            // Album Detail Header Banner
            Surface(
                color = themeColors.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // "← All Albums" button ABOVE the album info
                    OutlinedButton(
                        onClick = { onSelectAlbum(null) },
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text("← All Albums", fontSize = 11.sp, color = themeColors.textPrimary)
                    }

                    // Album info taking up the full horizontal space
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
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

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .combinedClickable(onClick = {}, onLongClick = { viewModel.setEditingAlbum(selectedAlbum) })
                        ) {
                            Text(
                                text = selectedAlbum.title,
                                fontSize = 18.sp,
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
            }

            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(albumSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        isMultiSelectMode = isMultiSelectMode,
                        isChecked = selectedSongIds.contains(song.id),
                        onCheckedChange = { viewModel.toggleSongSelection(song.id) },
                        onClick = {
                            if (isMultiSelectMode) viewModel.toggleSongSelection(song.id)
                            else viewModel.playSong(song, albumSongs)
                        },
                        onLongClick = { viewModel.toggleSongSelection(song.id) }
                    )
                }
            }
        }
    } else if (albums.isEmpty()) {
        EmptyMediaNotice("No Albums Found", "No album records present in library.")
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(if (themeColors.isAppleMusic) 16.dp else 0.dp)) {
            if (themeColors.isAppleMusic) {
                Text(
                    text = "Albums",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
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
}

@Composable
private fun ArtistsSubScreen(
    songs: List<SongEntity>,
    albums: List<AlbumEntity>,
    allArtists: List<ArtistEntity>,
    selectedArtist: String?,
    onSelectArtist: (String?) -> Unit,
    viewModel: MainViewModel
) {
    val themeColors = LocalThemeColors.current
    val artists = songs.map { it.artist }.filter { it.isNotBlank() && it != "Unknown Artist" }.distinct().sorted()
    val editingArtist by viewModel.editingArtist.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

    if (editingArtist != null) {
        val currentArt = allArtists.firstOrNull { it.name.equals(editingArtist, ignoreCase = true) }?.customArtworkUri ?: ""
        ArtistMetadataDialog(
            artistName = editingArtist!!,
            currentArtworkUri = currentArt,
            onDismiss = { viewModel.setEditingArtist(null) },
            onSave = { newName, artworkUri ->
                viewModel.updateArtistMetadata(editingArtist!!, newName, artworkUri)
            }
        )
    }

    var selectedArtistAlbum by remember(selectedArtist) { mutableStateOf<AlbumEntity?>(null) }

    if (selectedArtist != null) {
        if (selectedArtistAlbum != null) {
            val album = selectedArtistAlbum!!
            val albumSongs = songs.filter { it.albumTitle.equals(album.title, ignoreCase = true) }
                .sortedBy { it.trackNumber }
            val albumTime = formatDuration(albumSongs.sumOf { it.duration })

            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = themeColors.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedArtistAlbum = null },
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text("← $selectedArtist Albums", fontSize = 11.sp, color = themeColors.textPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(themeColors.background)
                                    .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (album.albumCoverUri.isNotBlank()) {
                                    AsyncImage(
                                        model = album.albumCoverUri,
                                        contentDescription = album.title,
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
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = album.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${album.albumArtist} • ${album.releaseYear}",
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
                }
                SongTableHeader()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(albumSongs) { index, song ->
                        SongTableRow(
                            song = song,
                            index = index,
                            isMultiSelectMode = isMultiSelectMode,
                            isChecked = selectedSongIds.contains(song.id),
                            onCheckedChange = { viewModel.toggleSongSelection(song.id) },
                            onClick = {
                                if (isMultiSelectMode) viewModel.toggleSongSelection(song.id)
                                else viewModel.playSong(song, albumSongs)
                            },
                            onLongClick = { viewModel.toggleSongSelection(song.id) }
                        )
                    }
                }
            }
            return
        }

        // Artist Albums List
        val artistAlbums = albums.filter { alb ->
            alb.albumArtist.equals(selectedArtist, ignoreCase = true) ||
                    songs.any { s -> s.artist.equals(selectedArtist, ignoreCase = true) && s.albumTitle.equals(alb.title, ignoreCase = true) }
        }.ifEmpty {
            songs.filter { it.artist.equals(selectedArtist, ignoreCase = true) }
                .map { it.albumTitle }
                .distinct()
                .map { title ->
                    val firstSong = songs.firstOrNull { it.albumTitle == title }
                    AlbumEntity(
                        title = title,
                        albumArtist = selectedArtist,
                        genre = firstSong?.genre ?: "Unspecified",
                        releaseYear = firstSong?.year ?: 2024,
                        albumCoverUri = firstSong?.artworkUri ?: ""
                    )
                }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = themeColors.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onSelectArtist(null) }) {
                        Text("← All Artists", fontSize = 11.sp, color = themeColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = selectedArtist, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                }
            }

            if (artistAlbums.isEmpty()) {
                EmptyMediaNotice("No Albums Found", "No albums recorded for $selectedArtist.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().background(themeColors.surface)) {
                    items(artistAlbums) { album ->
                        val count = songs.count { it.albumTitle.equals(album.title, ignoreCase = true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArtistAlbum = album }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(themeColors.background)
                                        .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (album.albumCoverUri.isNotBlank()) {
                                        AsyncImage(model = album.albumCoverUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(26.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = album.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                    Text(text = "${album.releaseYear} • $count Songs", fontSize = 12.sp, color = themeColors.textSecondary)
                                }
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                        HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                    }
                }
            }
        }
        return
    }

    if (themeColors.isAppleMusic) {
        if (artists.isEmpty()) {
            EmptyMediaNotice("No Artists Found", "Add music to populate your library.")
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Artists",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(artists) { artist ->
                        val artistEntity = allArtists.firstOrNull { it.name.equals(artist, ignoreCase = true) }
                        val artworkUri = artistEntity?.customArtworkUri?.takeIf { it.isNotBlank() } ?: run {
                            val artAlbums = albums.filter { alb ->
                                alb.albumArtist.equals(artist, ignoreCase = true) ||
                                        songs.any { s -> s.artist.equals(artist, ignoreCase = true) && s.albumTitle.equals(alb.title, ignoreCase = true) }
                            }.sortedBy { it.title }
                            artAlbums.firstOrNull { it.albumCoverUri.isNotBlank() }?.albumCoverUri ?: songs.firstOrNull { it.artist.equals(artist, ignoreCase = true) && it.artworkUri.isNotBlank() }?.artworkUri ?: ""
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = { onSelectArtist(artist) },
                                    onLongClick = { viewModel.setEditingArtist(artist) }
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.surface)
                                    .border(1.dp, themeColors.surfaceBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkUri.isNotBlank()) {
                                    AsyncImage(model = artworkUri, contentDescription = artist, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(38.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = artist,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // Classic iOS 6 Full-Width List for Legacy Themes
    if (artists.isEmpty()) {
        EmptyMediaNotice("No Artists Found", "Add music to populate your library.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().background(themeColors.surface)) {
            items(artists) { artist ->
                val artistEntity = allArtists.firstOrNull { it.name.equals(artist, ignoreCase = true) }
                val artworkUri = artistEntity?.customArtworkUri?.takeIf { it.isNotBlank() } ?: run {
                    val artAlbums = albums.filter { alb ->
                        alb.albumArtist.equals(artist, ignoreCase = true) ||
                                songs.any { s -> s.artist.equals(artist, ignoreCase = true) && s.albumTitle.equals(alb.title, ignoreCase = true) }
                    }.sortedBy { it.title }
                    artAlbums.firstOrNull { it.albumCoverUri.isNotBlank() }?.albumCoverUri ?: songs.firstOrNull { it.artist.equals(artist, ignoreCase = true) && it.artworkUri.isNotBlank() }?.artworkUri ?: ""
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onSelectArtist(artist) },
                            onLongClick = { viewModel.setEditingArtist(artist) }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(themeColors.background)
                                .border(0.5.dp, themeColors.surfaceBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (artworkUri.isNotBlank()) {
                                AsyncImage(model = artworkUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = artist,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
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
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

    if (selectedGenre != null) {
        val genreSongs = songs.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = themeColors.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onSelectGenre(null) }) {
                        Text("← All Genres", fontSize = 11.sp, color = themeColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = selectedGenre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                }
            }
            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(genreSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        isMultiSelectMode = isMultiSelectMode,
                        isChecked = selectedSongIds.contains(song.id),
                        onCheckedChange = { viewModel.toggleSongSelection(song.id) },
                        onClick = {
                            if (isMultiSelectMode) viewModel.toggleSongSelection(song.id)
                            else viewModel.playSong(song, genreSongs)
                        },
                        onLongClick = { viewModel.toggleSongSelection(song.id) }
                    )
                }
            }
        }
        return
    }

    if (genres.isEmpty()) {
        EmptyMediaNotice("No Genres Found", "Add music to populate your library.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().background(themeColors.surface)) {
            items(genres) { genre ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(genre) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = genre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
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
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

    if (selectedPlaylist != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = themeColors.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onSelectPlaylist(null) }) {
                        Text("← All Playlists", fontSize = 11.sp, color = themeColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = selectedPlaylist.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        Text(text = "Custom Playlist • ${allSongs.size} Songs", fontSize = 12.sp, color = themeColors.textSecondary)
                    }
                }
            }
            SongTableHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(allSongs) { index, song ->
                    SongTableRow(
                        song = song,
                        index = index,
                        isMultiSelectMode = isMultiSelectMode,
                        isChecked = selectedSongIds.contains(song.id),
                        onCheckedChange = { viewModel.toggleSongSelection(song.id) },
                        onClick = {
                            if (isMultiSelectMode) viewModel.toggleSongSelection(song.id)
                            else viewModel.playSong(song, allSongs)
                        },
                        onLongClick = { viewModel.toggleSongSelection(song.id) }
                    )
                }
            }
        }
        return
    }

    if (themeColors.isAppleMusic) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playlists",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.textPrimary
                )
                Button(
                    onClick = onCreatePlaylistClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Playlist...", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (playlists.isEmpty()) {
                EmptyMediaNotice("No Playlists", "Create your first playlist using the button above.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { playlist ->
                        var showEditDialog by remember { mutableStateOf(false) }

                        if (showEditDialog) {
                            EditPlaylistDialog(
                                playlist = playlist,
                                onDismiss = { showEditDialog = false },
                                onRename = { newTitle ->
                                    viewModel.renamePlaylist(playlist.id, newTitle)
                                    showEditDialog = false
                                },
                                onDelete = {
                                    viewModel.deletePlaylist(playlist)
                                    showEditDialog = false
                                }
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.surface,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onSelectPlaylist(playlist) },
                                    onLongClick = { showEditDialog = true }
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(themeColors.primaryAccent, Color(0xFF8B5CF6))
                                            )
                                        ),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = playlist.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Custom Playlist",
                                    fontSize = 11.sp,
                                    color = themeColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Classic iOS 6 Full-Width List for Legacy Themes
    LazyColumn(modifier = Modifier.fillMaxSize().background(themeColors.surface)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreatePlaylistClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "New Playlist...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent
                )
            }
            HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp)
        }

        items(playlists) { playlist ->
            var showEditDialog by remember { mutableStateOf(false) }

            if (showEditDialog) {
                EditPlaylistDialog(
                    playlist = playlist,
                    onDismiss = { showEditDialog = false },
                    onRename = { newTitle ->
                        viewModel.renamePlaylist(playlist.id, newTitle)
                        showEditDialog = false
                    },
                    onDelete = {
                        viewModel.deletePlaylist(playlist)
                        showEditDialog = false
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onSelectPlaylist(playlist) },
                        onLongClick = { showEditDialog = true }
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = playlist.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
        }
    }
}

@Composable
private fun MusicVideosSubScreen(
    videos: List<SongEntity>,
    viewModel: MainViewModel
) {
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()

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
                        isMultiSelectMode = isMultiSelectMode,
                        isChecked = selectedSongIds.contains(video.id),
                        onCheckedChange = { viewModel.toggleSongSelection(video.id) },
                        onClick = {
                            if (isMultiSelectMode) viewModel.toggleSongSelection(video.id)
                            else viewModel.playSong(video, videos)
                        },
                        onLongClick = { viewModel.toggleSongSelection(video.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMediaNotice(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val themeColors = LocalThemeColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = themeColors.textSecondary)
        }
    }
}

@Composable
fun EditPlaylistDialog(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    var titleInput by remember { mutableStateOf(playlist.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Playlist", fontWeight = FontWeight.Bold, color = themeColors.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
                Button(
                    onClick = { onRename(titleInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = themeColors.textSecondary)
            }
        },
        containerColor = themeColors.surface
    )
}

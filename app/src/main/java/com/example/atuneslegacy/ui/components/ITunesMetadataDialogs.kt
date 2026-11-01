package com.example.atuneslegacy.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.remote.MusicBrainzApi
import com.example.atuneslegacy.data.remote.TmdApi
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import kotlinx.coroutines.launch

@Composable
fun AutoSuggestOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    val themeColors = LocalThemeColors.current
    var expanded by remember { mutableStateOf(false) }

    // Suggest info when value length >= 3
    val filteredSuggestions = remember(value, suggestions) {
        if (value.length >= 3) {
            suggestions.filter {
                it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true)
            }.distinct().take(5)
        } else {
            emptyList()
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.length >= 3
            },
            label = { Text(label) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(color = themeColors.inputText),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = themeColors.inputText,
                unfocusedTextColor = themeColors.inputText,
                focusedContainerColor = themeColors.surface,
                unfocusedContainerColor = themeColors.surface,
                focusedBorderColor = themeColors.primaryAccent,
                unfocusedBorderColor = themeColors.surfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded && filteredSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .background(themeColors.surface)
                .border(1.dp, themeColors.surfaceBorder)
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = suggestion,
                            color = themeColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ITunesDialogContainer(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
    showPullMetadata: Boolean = false,
    onPullMetadata: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val themeColors = LocalThemeColors.current

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = themeColors.surface,
        border = BorderStroke(1.dp, themeColors.surfaceBorder),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (themeColors.isDark) {
                                listOf(Color(0xFF2C3038), Color(0xFF1E2125))
                            } else {
                                listOf(Color(0xFFE2E6EE), Color(0xFFC4C9D4))
                            }
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(themeColors.surfaceBorder)
            )

            // Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                content()

                if (showPullMetadata && onPullMetadata != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onPullMetadata,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Pull Metadata",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pull Metadata from Online Database", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Footer Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(themeColors.surfaceBorder)
                )

                // Footer Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.background)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(0.9f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Delete", fontSize = 11.sp, color = Color(0xFFDC2626), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary),
                        border = BorderStroke(1.dp, themeColors.surfaceBorder),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("Save Changes", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// 1. SONG / MUSIC VIDEO METADATA DIALOG
@Composable
fun SongMetadataDialog(
    song: SongEntity,
    artistSuggestions: List<String> = emptyList(),
    albumSuggestions: List<String> = emptyList(),
    genreSuggestions: List<String> = emptyList(),
    composerSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (SongEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(song) { mutableStateOf(song.title) }
    var artist by remember(song) { mutableStateOf(song.artist) }
    var albumArtist by remember(song) { mutableStateOf(song.albumArtist) }
    var albumTitle by remember(song) { mutableStateOf(song.albumTitle) }
    var composer by remember(song) { mutableStateOf(song.composer) }
    var genre by remember(song) { mutableStateOf(song.genre) }
    var year by remember(song) { mutableStateOf(song.year.toString()) }
    var trackNumber by remember(song) { mutableStateOf(song.trackNumber.toString()) }
    var artworkUri by remember(song) { mutableStateOf(song.artworkUri) }

    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { artworkUri = it.toString() }
    }

    ITunesDialogContainer(
        title = if (song.isMusicVideo) "Edit Music Video Info" else "Edit Song Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                song.copy(
                    title = title.trim(),
                    artist = artist.trim(),
                    albumArtist = albumArtist.trim(),
                    albumTitle = albumTitle.trim(),
                    composer = composer.trim(),
                    genre = genre.trim(),
                    year = year.toIntOrNull() ?: song.year,
                    trackNumber = trackNumber.toIntOrNull() ?: song.trackNumber,
                    artworkUri = artworkUri
                )
            )
        },
        showPullMetadata = true,
        onPullMetadata = {
            scope.launch {
                val res = MusicBrainzApi.searchRecording("$artist $title") ?: MusicBrainzApi.searchRecording(title)
                if (res != null) {
                    title = res.title
                    artist = res.artist
                    albumArtist = res.artist
                    albumTitle = res.albumTitle
                    year = res.year.toString()
                    if (!res.artworkUrl.isNullOrBlank()) {
                        artworkUri = res.artworkUrl
                    }
                } else {
                    artist = artist.ifBlank { "Pop / Rock Artist" }
                    albumArtist = albumArtist.ifBlank { artist }
                    albumTitle = albumTitle.ifBlank { "$title - Single" }
                    genre = genre.ifBlank { "Pop" }
                    year = if (year == "0") "2024" else year
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkUri.isNotBlank()) {
                        AsyncImage(
                            model = artworkUri,
                            contentDescription = "Artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Artwork...", fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (song.isMusicVideo) "Video Title" else "Song Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AutoSuggestOutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = "Artist",
                suggestions = artistSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = albumArtist,
                onValueChange = { albumArtist = it },
                label = "Album Artist",
                suggestions = artistSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = albumTitle,
                onValueChange = { albumTitle = it },
                label = "Album Title",
                suggestions = albumSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = composer,
                onValueChange = { composer = it },
                label = "Composer",
                suggestions = composerSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = "Genre",
                suggestions = genreSuggestions
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = trackNumber,
                    onValueChange = { trackNumber = it },
                    label = { Text("Track #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 2. ALBUM METADATA DIALOG (Cascading)
@Composable
fun AlbumMetadataDialog(
    album: AlbumEntity,
    artistSuggestions: List<String> = emptyList(),
    albumSuggestions: List<String> = emptyList(),
    genreSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (oldTitle: String, AlbumEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val oldTitle = album.title
    var title by remember(album) { mutableStateOf(album.title) }
    var albumArtist by remember(album) { mutableStateOf(album.albumArtist) }
    var genre by remember(album) { mutableStateOf(album.genre) }
    var releaseYear by remember(album) { mutableStateOf(album.releaseYear.toString()) }
    var coverUri by remember(album) { mutableStateOf(album.albumCoverUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { coverUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Album Info (Cascades to Songs)",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                oldTitle,
                album.copy(
                    title = title.trim(),
                    albumArtist = albumArtist.trim(),
                    genre = genre.trim(),
                    releaseYear = releaseYear.toIntOrNull() ?: album.releaseYear,
                    albumCoverUri = coverUri
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri.isNotBlank()) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Artwork...", fontSize = 12.sp)
                }
            }

            AutoSuggestOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Album Title",
                suggestions = albumSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = albumArtist,
                onValueChange = { albumArtist = it },
                label = "Album Artist",
                suggestions = artistSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = "Genre",
                suggestions = genreSuggestions
            )
            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text("Release Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 3. TV SHOW METADATA DIALOG (Cascading)
@Composable
fun TvShowMetadataDialog(
    show: TvShowEntity,
    showSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (oldShowTitle: String, TvShowEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val oldTitle = show.title
    var title by remember(show) { mutableStateOf(show.title) }
    var releaseYear by remember(show) { mutableStateOf(show.releaseYear.toString()) }
    var parentalRating by remember(show) { mutableStateOf(show.parentalRating) }
    var logoUri by remember(show) { mutableStateOf(show.logoThumbnailUri) }
    var description by remember(show) { mutableStateOf(show.description) }
    var starRating by remember(show) { mutableIntStateOf(show.starRating) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { logoUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit TV Show Info (Cascades to Episodes)",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                oldTitle,
                show.copy(
                    title = title.trim(),
                    releaseYear = releaseYear.toIntOrNull() ?: show.releaseYear,
                    parentalRating = parentalRating.trim(),
                    logoThumbnailUri = logoUri,
                    description = description.trim(),
                    starRating = starRating
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri.isNotBlank()) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Show Logo...", fontSize = 12.sp)
                }
            }

            AutoSuggestOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Show Title",
                suggestions = showSuggestions
            )
            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text("Release Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = parentalRating,
                onValueChange = { parentalRating = it },
                label = { Text("Parental Rating (e.g. TV-MA, TV-14)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Star Rating (0-5 stars)
            Column {
                Text("Star Rating", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        val isSelected = i <= starRating
                        Text(
                            text = if (isSelected) "★" else "☆",
                            fontSize = 24.sp,
                            color = if (isSelected) Color(0xFFEAB308) else Color.Gray,
                            modifier = Modifier
                                .clickable { starRating = i }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// MOVIE METADATA DIALOG
@Composable
fun MovieMetadataDialog(
    movie: MovieEntity,
    artistSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (MovieEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(movie) { mutableStateOf(movie.title) }
    var artists by remember(movie) { mutableStateOf(movie.artists) }
    var releaseYear by remember(movie) { mutableStateOf(movie.releaseYear.toString()) }
    var parentalRating by remember(movie) { mutableStateOf(movie.parentalRating) }
    var posterUri by remember(movie) { mutableStateOf(movie.thumbnailUri) }
    var description by remember(movie) { mutableStateOf(movie.description) }
    var starRating by remember(movie) { mutableIntStateOf(movie.starRating) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { posterUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Movie Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                movie.copy(
                    title = title.trim(),
                    artists = artists.trim(),
                    releaseYear = releaseYear.toIntOrNull() ?: movie.releaseYear,
                    parentalRating = parentalRating.trim(),
                    thumbnailUri = posterUri,
                    description = description.trim(),
                    starRating = starRating
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (posterUri.isNotBlank()) {
                        AsyncImage(
                            model = posterUri,
                            contentDescription = "Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Movie Poster...", fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Movie Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AutoSuggestOutlinedTextField(
                value = artists,
                onValueChange = { artists = it },
                label = "Cast / Artists",
                suggestions = artistSuggestions
            )
            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text("Release Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = parentalRating,
                onValueChange = { parentalRating = it },
                label = { Text("Parental Rating (e.g. PG-13)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Star Rating (0-5 stars)
            Column {
                Text("Star Rating", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        val isSelected = i <= starRating
                        Text(
                            text = if (isSelected) "★" else "☆",
                            fontSize = 24.sp,
                            color = if (isSelected) Color(0xFFEAB308) else Color.Gray,
                            modifier = Modifier
                                .clickable { starRating = i }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// 4. TV EPISODE METADATA DIALOG
@Composable
fun TvEpisodeMetadataDialog(
    episode: TvEpisodeEntity,
    showSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TvEpisodeEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(episode) { mutableStateOf(episode.title) }
    var showTitle by remember(episode) { mutableStateOf(episode.showTitle) }
    var seasonNumber by remember(episode) { mutableStateOf(episode.seasonNumber.toString()) }
    var episodeNumber by remember(episode) { mutableStateOf(episode.episodeNumber.toString()) }
    var thumbnailUri by remember(episode) { mutableStateOf(episode.thumbnailUri) }

    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { thumbnailUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit TV Episode Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                episode.copy(
                    title = title.trim(),
                    showTitle = showTitle.trim(),
                    seasonNumber = seasonNumber.toIntOrNull() ?: episode.seasonNumber,
                    episodeNumber = episodeNumber.toIntOrNull() ?: episode.episodeNumber,
                    thumbnailUri = thumbnailUri
                )
            )
        },
        showPullMetadata = true,
        onPullMetadata = {
            scope.launch {
                val res = TmdApi.searchTvShow(showTitle)
                if (res != null) {
                    if (showTitle.isBlank()) showTitle = res.name ?: showTitle
                    if (seasonNumber == "0") seasonNumber = "1"
                    if (episodeNumber == "0") episodeNumber = "1"
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailUri.isNotBlank()) {
                        AsyncImage(
                            model = thumbnailUri,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Thumbnail...", fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Episode Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AutoSuggestOutlinedTextField(
                value = showTitle,
                onValueChange = { showTitle = it },
                label = "Show Name",
                suggestions = showSuggestions
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = seasonNumber,
                    onValueChange = { seasonNumber = it },
                    label = { Text("Season #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = episodeNumber,
                    onValueChange = { episodeNumber = it },
                    label = { Text("Episode #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 5. MOVIE METADATA DIALOG
@Composable
fun MovieMetadataDialog(
    movie: MovieEntity,
    onDismiss: () -> Unit,
    onSave: (MovieEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(movie) { mutableStateOf(movie.title) }
    var artists by remember(movie) { mutableStateOf(movie.artists) }
    var releaseYear by remember(movie) { mutableStateOf(movie.releaseYear.toString()) }
    var parentalRating by remember(movie) { mutableStateOf(movie.parentalRating) }
    var thumbnailUri by remember(movie) { mutableStateOf(movie.thumbnailUri) }

    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { thumbnailUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Movie Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                movie.copy(
                    title = title.trim(),
                    artists = artists.trim(),
                    releaseYear = releaseYear.toIntOrNull() ?: movie.releaseYear,
                    parentalRating = parentalRating.trim(),
                    thumbnailUri = thumbnailUri
                )
            )
        },
        showPullMetadata = true,
        onPullMetadata = {
            scope.launch {
                val res = TmdApi.searchMovie(title)
                if (res != null) {
                    title = res.title ?: title
                    if (!res.release_date.isNullOrBlank() && res.release_date.length >= 4) {
                        releaseYear = res.release_date.take(4)
                    }
                    if (!res.overview.isNullOrBlank()) {
                        artists = res.overview.take(70) + "..."
                    }
                    if (!res.poster_path.isNullOrBlank()) {
                        thumbnailUri = TmdApi.getImageUrl(res.poster_path)
                    }
                    parentalRating = "PG-13"
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailUri.isNotBlank()) {
                        AsyncImage(
                            model = thumbnailUri,
                            contentDescription = "Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.7f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Movie Poster...", fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Movie Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artists,
                onValueChange = { artists = it },
                label = { Text("Cast / Artists") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text("Release Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = parentalRating,
                onValueChange = { parentalRating = it },
                label = { Text("Parental Rating (e.g. PG-13, R)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 6. PODCAST SHOW METADATA DIALOG
@Composable
fun PodcastShowMetadataDialog(
    show: PodcastShowEntity,
    podcastSuggestions: List<String> = emptyList(),
    creatorSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (oldPodcastName: String, PodcastShowEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val oldTitle = show.title
    var title by remember(show) { mutableStateOf(show.title) }
    var creatorHost by remember(show) { mutableStateOf(show.creatorHost) }
    var description by remember(show) { mutableStateOf(show.description) }
    var category by remember(show) { mutableStateOf(show.category) }
    var logoUri by remember(show) { mutableStateOf(show.logoThumbnailUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { logoUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Podcast Info (Cascades to Episodes)",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                oldTitle,
                show.copy(
                    title = title.trim(),
                    creatorHost = creatorHost.trim(),
                    description = description.trim(),
                    category = category.trim(),
                    logoThumbnailUri = logoUri
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri.isNotBlank()) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Artwork...", fontSize = 12.sp)
                }
            }

            AutoSuggestOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Podcast Title",
                suggestions = podcastSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = creatorHost,
                onValueChange = { creatorHost = it },
                label = "Creator / Host",
                suggestions = creatorSuggestions
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (e.g. Technology, News)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 7. PODCAST EPISODE METADATA DIALOG
@Composable
fun PodcastEpisodeMetadataDialog(
    episode: PodcastEpisodeEntity,
    podcastSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (PodcastEpisodeEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(episode) { mutableStateOf(episode.title) }
    var podcastName by remember(episode) { mutableStateOf(episode.podcastName) }
    var seasonNumber by remember(episode) { mutableStateOf(episode.seasonNumber.toString()) }
    var episodeNumber by remember(episode) { mutableStateOf(episode.episodeNumber.toString()) }

    ITunesDialogContainer(
        title = "Edit Podcast Episode Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                episode.copy(
                    title = title.trim(),
                    podcastName = podcastName.trim(),
                    seasonNumber = seasonNumber.toIntOrNull() ?: episode.seasonNumber,
                    episodeNumber = episodeNumber.toIntOrNull() ?: episode.episodeNumber
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Episode Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AutoSuggestOutlinedTextField(
                value = podcastName,
                onValueChange = { podcastName = it },
                label = "Podcast Name",
                suggestions = podcastSuggestions
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = seasonNumber,
                    onValueChange = { seasonNumber = it },
                    label = { Text("Season #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = episodeNumber,
                    onValueChange = { episodeNumber = it },
                    label = { Text("Episode #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 8. AUDIOBOOK METADATA DIALOG
@Composable
fun AudiobookMetadataDialog(
    book: AudiobookEntity,
    authorSuggestions: List<String> = emptyList(),
    seriesSuggestions: List<String> = emptyList(),
    genreSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (AudiobookEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(book) { mutableStateOf(book.title) }
    var author by remember(book) { mutableStateOf(book.author) }
    var bookSeries by remember(book) { mutableStateOf(book.bookSeries) }
    var bookNumber by remember(book) { mutableStateOf(book.bookNumberInSeries.toString()) }
    var genre by remember(book) { mutableStateOf(book.genre) }
    var pubYear by remember(book) { mutableStateOf(book.publicationYear.toString()) }
    var coverUri by remember(book) { mutableStateOf(book.portraitCoverUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { coverUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Audiobook Info",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                book.copy(
                    title = title.trim(),
                    author = author.trim(),
                    bookSeries = bookSeries.trim(),
                    bookNumberInSeries = bookNumber.toIntOrNull() ?: book.bookNumberInSeries,
                    genre = genre.trim(),
                    publicationYear = pubYear.toIntOrNull() ?: book.publicationYear,
                    portraitCoverUri = coverUri
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri.isNotBlank()) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.68f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Portrait Cover...", fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Book Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AutoSuggestOutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = "Author",
                suggestions = authorSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = bookSeries,
                onValueChange = { bookSeries = it },
                label = "Book Series",
                suggestions = seriesSuggestions
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bookNumber,
                    onValueChange = { bookNumber = it },
                    label = { Text("Book # in Series") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pubYear,
                    onValueChange = { pubYear = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            AutoSuggestOutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = "Genre",
                suggestions = genreSuggestions
            )
        }
    }
}

// 9. BOOK SERIES METADATA DIALOG (Cascading)
@Composable
fun BookSeriesMetadataDialog(
    series: BookSeriesEntity,
    seriesSuggestions: List<String> = emptyList(),
    genreSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (oldSeriesTitle: String, BookSeriesEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val oldTitle = series.title
    var title by remember(series) { mutableStateOf(series.title) }
    var genre by remember(series) { mutableStateOf(series.genre) }
    var coverUri by remember(series) { mutableStateOf(series.portraitCoverUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { coverUri = it.toString() }
    }

    ITunesDialogContainer(
        title = "Edit Book Series Info (Cascades to Books)",
        onDismiss = onDismiss,
        onDelete = onDelete,
        onSave = {
            onSave(
                oldTitle,
                series.copy(
                    title = title.trim(),
                    genre = genre.trim(),
                    portraitCoverUri = coverUri
                )
            )
        },
        showPullMetadata = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri.isNotBlank()) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.68f)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                }
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Select Portrait Cover...", fontSize = 12.sp)
                }
            }

            AutoSuggestOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = "Series Title",
                suggestions = seriesSuggestions
            )
            AutoSuggestOutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = "Genre",
                suggestions = genreSuggestions
            )
        }
    }
}

package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.AudiobookChapter
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.ui.components.AudiobookCard
import com.example.atuneslegacy.ui.components.formatAudiobookPosition
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.navigation.AudiobookSubTab
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import java.io.File
import kotlinx.serialization.json.Json

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudiobooksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSubTab by viewModel.audiobookSubTab.collectAsState()
    val audiobooks by viewModel.allAudiobooks.collectAsState()
    val bookSeriesList by viewModel.allBookSeries.collectAsState()

    var selectedAuthorFilter by remember { mutableStateOf<String?>(null) }
    var selectedSeriesFilter by remember { mutableStateOf<String?>(null) }
    var selectedGenreFilter by remember { mutableStateOf<String?>(null) }
    var selectedBookForDetail by remember { mutableStateOf<AudiobookEntity?>(null) }

    val activeDetailBook = selectedBookForDetail?.let { book ->
        audiobooks.firstOrNull { it.id == book.id } ?: book
    }

    if (activeDetailBook != null) {
        AudiobookDetailScreen(
            book = activeDetailBook,
            viewModel = viewModel,
            onBack = { selectedBookForDetail = null }
        )
        return
    }

    val woodGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFE2A86D),
            Color(0xFFC48B52),
            Color(0xFF9E6333)
        )
    )

    Column(modifier = modifier.fillMaxSize().background(woodGradient)) {
        // Sub-Tab Switcher (Books, Authors, Series, Genres)
        ITunesSegmentedControl(
            items = AudiobookSubTab.entries,
            selectedItem = currentSubTab,
            onItemSelected = { viewModel.selectAudiobookSubTab(it) },
            getItemLabel = { it.title }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(woodGradient)) {
            when (currentSubTab) {
                AudiobookSubTab.BOOKS -> {
                    WoodenBookshelfScreen(
                        audiobooks = audiobooks,
                        viewModel = viewModel,
                        onBookClick = { selectedBookForDetail = it }
                    )
                }
                AudiobookSubTab.AUTHORS -> {
                    val authors = audiobooks.map { it.author }.filter { it.isNotBlank() }.distinct().sorted()

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
                                        .clickable { selectedAuthorFilter = null }
                                        .background(if (selectedAuthorFilter == null) ITunesAccentBlue else Color.Transparent)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "All Authors (${authors.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedAuthorFilter == null) Color.White else Color(0xFF2C3038)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFD0D4DD))
                            }
                            items(authors) { author ->
                                val isSelected = author.equals(selectedAuthorFilter, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAuthorFilter = author }
                                        .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = author,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF2C3038)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFE0E3EA))
                            }
                        }

                        val filteredBooks = if (selectedAuthorFilter == null) audiobooks else audiobooks.filter { it.author.equals(selectedAuthorFilter, ignoreCase = true) }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredBooks) { book ->
                                AudiobookCard(
                                    book = book,
                                    onClick = { selectedBookForDetail = book },
                                    onLongClick = { viewModel.setEditingAudiobook(book) }
                                )
                            }
                        }
                    }
                }
                AudiobookSubTab.SERIES -> {
                    val allSeriesNames = (bookSeriesList.map { it.title } + audiobooks.map { it.bookSeries })
                        .filter { it.isNotBlank() && !it.equals("Unspecified", ignoreCase = true) }
                        .distinct()
                        .sorted()

                    if (allSeriesNames.isEmpty()) {
                        EmptyMediaNotice("No Series", "No audiobook series found.")
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Series List
                            LazyColumn(
                                modifier = Modifier
                                    .width(170.dp)
                                    .fillMaxSize()
                                    .background(Color(0xFFE8EBF0))
                                    .border(width = 0.5.dp, color = Color(0xFFC0C5CE))
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSeriesFilter = null }
                                            .background(if (selectedSeriesFilter == null) ITunesAccentBlue else Color.Transparent)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "All Series (${allSeriesNames.size})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedSeriesFilter == null) Color.White else Color(0xFF2C3038)
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFFD0D4DD))
                                }
                                items(allSeriesNames) { seriesName ->
                                    val isSelected = seriesName.equals(selectedSeriesFilter, ignoreCase = true)
                                    val count = audiobooks.count { it.bookSeries.equals(seriesName, ignoreCase = true) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSeriesFilter = seriesName }
                                            .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = seriesName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF2C3038),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$count ${if (count == 1) "book" else "books"}",
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF687080)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFE0E3EA))
                                }
                            }

                            // Right Books Grid
                            val filteredBooks = if (selectedSeriesFilter == null) {
                                audiobooks.filter { it.bookSeries.isNotBlank() && !it.bookSeries.equals("Unspecified", ignoreCase = true) }
                            } else {
                                audiobooks.filter { it.bookSeries.equals(selectedSeriesFilter, ignoreCase = true) }
                            }.sortedBy { it.bookNumberInSeries }

                            if (filteredBooks.isEmpty()) {
                                EmptyMediaNotice("No Books in Series", "No audiobooks found for this series.")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 120.dp),
                                    contentPadding = PaddingValues(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredBooks) { book ->
                                        AudiobookCard(
                                            book = book,
                                            onClick = { selectedBookForDetail = book },
                                            onLongClick = { viewModel.setEditingAudiobook(book) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                AudiobookSubTab.GENRES -> {
                    val genres = audiobooks.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()
                    if (genres.isEmpty()) {
                        EmptyMediaNotice("No Genres", "No audiobook genres found.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
                            items(genres) { genre ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1D21)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun AudiobookDetailScreen(
    book: AudiobookEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentMedia by viewModel.mediaPlayerManager.currentMedia.collectAsState()
    val isPlaying by viewModel.mediaPlayerManager.isPlaying.collectAsState()
    val currentPositionMs by viewModel.mediaPlayerManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.mediaPlayerManager.durationMs.collectAsState()

    val isThisBookPlaying = currentMedia?.id == "book_${book.id}"
    val effectivePosition = if (isThisBookPlaying) currentPositionMs else book.lastPlaybackPositionMs

    val chapters = remember(book.chaptersJson) {
        if (book.chaptersJson.isBlank()) {
            listOf(AudiobookChapter("00:00:00", 0L, book.title, 1))
        } else {
            try {
                Json.decodeFromString<List<AudiobookChapter>>(book.chaptersJson)
            } catch (e: Exception) {
                listOf(AudiobookChapter("00:00:00", 0L, book.title, 1))
            }
        }
    }

    val currentChapterIndex = remember(chapters, effectivePosition) {
        var activeIdx = 0
        for (i in chapters.indices) {
            if (effectivePosition >= chapters[i].timeMs) {
                activeIdx = i
            } else {
                break
            }
        }
        activeIdx
    }

    val playIconBitmap = remember {
        try {
            android.graphics.BitmapFactory.decodeFile("/home/TheBluestPuppy/Downloads/play.png")?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val totalDurationMs = remember(chapters, durationMs, isThisBookPlaying) {
        if (isThisBookPlaying && durationMs > 0) {
            durationMs
        } else {
            chapters.lastOrNull()?.timeMs?.takeIf { it > 0 } ?: 3600000L
        }
    }

    val progressFraction = if (totalDurationMs > 0) (effectivePosition.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val progressPercent = (progressFraction * 100).toInt()

    val screenBg = Brush.verticalGradient(
        listOf(
            Color(0xFFEAEFF5),
            Color(0xFFD0D7E5),
            Color(0xFFB5BECE)
        )
    )

    Column(modifier = Modifier.fillMaxSize().background(screenBg)) {
        // Top Bar (iOS 6 style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF91A5CA), Color(0xFF5E759E), Color(0xFF3E547D))
                    )
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button ("Audiobooks")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2C4166),
                border = BorderStroke(1.dp, Color(0xFF1B2A45)),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Audiobooks",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title ("Book Info")
            Text(
                text = "Book Info",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Now Playing Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2C4166),
                border = BorderStroke(1.dp, Color(0xFF1B2A45)),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable { viewModel.mediaPlayerManager.showMusicPlayer() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Now Playing",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Now Playing",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Main Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Book Header Card (Cover + Title + Author + Description)
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAFAFB),
                    border = BorderStroke(1.dp, Color(0xFFB0BACB)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Cover Image
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(0.5.dp, Color(0xFF7F8C8D)),
                            modifier = Modifier
                                .width(110.dp)
                                .height(160.dp)
                        ) {
                            if (book.portraitCoverUri.isNotBlank()) {
                                AsyncImage(
                                    model = book.portraitCoverUri,
                                    contentDescription = book.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFF2C3E50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }

                        // Title, Author, Divider, Description
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = book.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C2833),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = book.author.ifBlank { "Unknown Author" },
                                fontSize = 13.sp,
                                color = Color(0xFF7F8C8D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            HorizontalDivider(color = Color(0xFFD5D8DC), thickness = 1.dp)
                            Text(
                                text = book.description.ifBlank { "No description provided for this audiobook." },
                                fontSize = 12.sp,
                                color = Color(0xFF2C3E50),
                                lineHeight = 16.sp,
                                maxLines = 7,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Progress Bar Card
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE2E7F0),
                    border = BorderStroke(1.dp, Color(0xFFB0BACB)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Progress:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF2C3E50)
                        )

                        // Custom Progress Bar Track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFCBD2E0))
                                .border(0.5.dp, Color(0xFF9AA5B8), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF70A5F8), Color(0xFF2368D4))
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "$progressPercent%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF2C3E50)
                        )
                    }
                }
            }

            // 3. Play Book Action Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF3379D5),
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, Color(0xFF11438B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                viewModel.playAudiobook(book)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF74A9EC), Color(0xFF3379D5), Color(0xFF1B5EB9))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (book.lastPlaybackPositionMs > 0) "Resume Book" else "Play Book",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 4. Connected Chapters Card (Header + Thin Connected Rows)
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFB0BACB)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF7E8EA8), Color(0xFF53637D))
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Chapters",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Chapter Rows Connected
                        chapters.forEachIndexed { index, chapter ->
                            val isCurrentChapter = isThisBookPlaying && (index == currentChapterIndex)

                            Surface(
                                shape = RoundedCornerShape(0.dp),
                                color = if (isCurrentChapter) Color(0xFFE0F2FE) else Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.playAudiobookAtChapter(book, chapter)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Circular Play Button with play.png icon
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF2A72E4),
                                            shadowElevation = 1.dp,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                if (playIconBitmap != null) {
                                                    Image(
                                                        bitmap = playIconBitmap,
                                                        contentDescription = "Play Chapter",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Play Chapter",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = chapter.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1C2833),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = chapter.time,
                                                fontSize = 10.sp,
                                                color = Color(0xFF7F8C8D)
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Go",
                                        tint = Color(0xFF95A5A6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (index < chapters.size - 1) {
                                HorizontalDivider(color = Color(0xFFE2E7F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WoodenShelfView() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Shelf Top Bevel Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFFFDE4CB))
        )
        // Main 3D Shelf Plank Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFD49B61),
                            Color(0xFFB57C42),
                            Color(0xFF6B3E14)
                        )
                    )
                )
        )
        // Shelf Drop Shadow underneath
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WoodenBookshelfScreen(
    audiobooks: List<AudiobookEntity>,
    viewModel: MainViewModel,
    onBookClick: (AudiobookEntity) -> Unit
) {
    val cabinetBackground = Brush.verticalGradient(
        listOf(
            Color(0xFFC48B52),
            Color(0xFF9E6333),
            Color(0xFF7A4518)
        )
    )

    val leftPanelGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF3F230A),
            Color(0xFF7A4518),
            Color(0xFFB57C42)
        )
    )
    val rightPanelGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFFB57C42),
            Color(0xFF7A4518),
            Color(0xFF3F230A)
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(cabinetBackground)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Cabinet Wall
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
                    .background(leftPanelGradient)
            )

            // Bookshelf Interior spanning between cabinet walls
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(cabinetBackground)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        if (audiobooks.isEmpty()) {
                            items(4) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(135.dp))
                                    WoodenShelfView()
                                }
                            }
                        } else {
                            val rows = audiobooks.chunked(3)
                            items(rows) { rowBooks ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        for (book in rowBooks) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .combinedClickable(
                                                        onClick = { onBookClick(book) },
                                                        onLongClick = { viewModel.setEditingAudiobook(book) }
                                                    )
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 4.dp, bottomEnd = 4.dp, bottomStart = 2.dp),
                                                    shadowElevation = 10.dp,
                                                    color = Color(0xFFE4E8F0),
                                                    border = BorderStroke(0.5.dp, Color(0xFF222222).copy(alpha = 0.5f)),
                                                    modifier = Modifier
                                                        .width(95.dp)
                                                        .height(135.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        if (book.portraitCoverUri.isNotBlank()) {
                                                            AsyncImage(
                                                                model = book.portraitCoverUri,
                                                                contentDescription = book.title,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize().background(Color(0xFF2C3038)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Book,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(32.dp)
                                                                )
                                                            }
                                                        }
                                                        if (book.lastPlaybackPositionMs > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.BottomCenter)
                                                                    .fillMaxWidth()
                                                                    .background(Color.Black.copy(alpha = 0.75f))
                                                                    .padding(vertical = 2.dp, horizontal = 2.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "Resume ${formatAudiobookPosition(book.lastPlaybackPositionMs)}",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF5BC0BE),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        repeat(3 - rowBooks.size) {
                                            Spacer(modifier = Modifier.width(95.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    WoodenShelfView()
                                }
                            }
                        }
                    }

                    if (audiobooks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(text = "No Audiobooks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Place audiobook files in\naTunes Library/Audiobooks/",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right Cabinet Wall
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
                    .background(rightPanelGradient)
            )
        }
    }
}

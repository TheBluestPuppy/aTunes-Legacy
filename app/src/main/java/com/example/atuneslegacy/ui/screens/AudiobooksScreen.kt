package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.ui.components.AudiobookCard
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.navigation.AudiobookSubTab
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun AudiobooksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {val context = LocalContext.current
    val currentSubTab by viewModel.audiobookSubTab.collectAsState()
    val audiobooks by viewModel.allAudiobooks.collectAsState()
    val bookSeriesList by viewModel.allBookSeries.collectAsState()

    var selectedAuthorFilter by remember { mutableStateOf<String?>(null) }
    var selectedGenreFilter by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        // Sub-Tab Switcher (Books, Authors, Series, Genres)
        ITunesSegmentedControl(
            items = AudiobookSubTab.entries,
            selectedItem = currentSubTab,
            onItemSelected = { viewModel.selectAudiobookSubTab(it) },
            getItemLabel = { it.title }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentSubTab) {
                AudiobookSubTab.BOOKS -> {
                    if (audiobooks.isEmpty()) {
                        EmptyMediaNotice("No Audiobooks", "No audiobook files in library.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(audiobooks) { book ->
                                AudiobookCard(
                                    book = book,
                                    onClick = { viewModel.playAudiobook(book) },
                                    onLongClick = { viewModel.setEditingAudiobook(book) }
                                )

                            }
                        }
                    }
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
                            modifier = Modifier.weight(1f).fillMaxSize()
                        ) {
                            items(filteredBooks) { book ->
                                AudiobookCard(
                                    book = book,
                                    onClick = { viewModel.playAudiobook(book) },
                                    onLongClick = { viewModel.setEditingAudiobook(book) }
                                )

                            }
                        }
                    }
                }
                AudiobookSubTab.SERIES -> {
                    if (bookSeriesList.isEmpty()) {
                        EmptyMediaNotice("No Series Found", "No audiobook series defined.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 130.dp),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(bookSeriesList) { series ->
                                BookSeriesCard(
                                    series = series,
                                    onClick = {},
                                    onLongClick = { viewModel.setEditingBookSeries(series) }
                                )
                            }
                        }
                    }
                }
                AudiobookSubTab.GENRES -> {
                    val genres = audiobooks.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()

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
                                        .clickable { selectedGenreFilter = null }
                                        .background(if (selectedGenreFilter == null) ITunesAccentBlue else Color.Transparent)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "All Genres (${genres.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedGenreFilter == null) Color.White else Color(0xFF2C3038)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFD0D4DD))
                            }
                            items(genres) { genre ->
                                val isSelected = genre.equals(selectedGenreFilter, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedGenreFilter = genre }
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

                        val filteredBooks = if (selectedGenreFilter == null) audiobooks else audiobooks.filter { it.genre.equals(selectedGenreFilter, ignoreCase = true) }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.weight(1f).fillMaxSize()
                        ) {
                            items(filteredBooks) { book ->
                                AudiobookCard(
                                    book = book,
                                    onClick = { viewModel.playAudiobook(book) },
                                    onLongClick = { viewModel.setEditingAudiobook(book) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookSeriesCard(
    series: BookSeriesEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E4EC))
                .border(1.dp, Color(0xFFB8BEC9), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (series.portraitCoverUri.isNotBlank()) {
                AsyncImage(
                    model = series.portraitCoverUri,
                    contentDescription = series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.72f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = series.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1D21),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = series.genre.ifBlank { "Series" },
            fontSize = 11.sp,
            color = Color(0xFF636A78),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

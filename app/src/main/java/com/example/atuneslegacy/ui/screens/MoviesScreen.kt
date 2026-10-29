package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.atuneslegacy.ui.components.MovieCard
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun MoviesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val movies by viewModel.allMovies.collectAsState()
    val isGridView by viewModel.isMovieGridView.collectAsState()
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        // Toolbar with Grid/List Toggle Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Movies (${movies.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3038)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (!isGridView) viewModel.toggleMovieGridView() }) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Grid View",
                        tint = if (isGridView) ITunesAccentBlue else Color(0xFF888E9A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { if (isGridView) viewModel.toggleMovieGridView() }) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "List View",
                        tint = if (!isGridView) ITunesAccentBlue else Color(0xFF888E9A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (movies.isEmpty()) {
            EmptyMediaNotice("No Movies in Library", "Import movie files to view them here.")
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            isGrid = true,
                            onClick = { viewModel.playMovie(movie) },
                            onLongClick = {
                                viewModel.setEditingMovie(movie)
                            }
                        )

                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            isGrid = false,
                            onClick = { viewModel.playMovie(movie) },
                            onLongClick = { viewModel.setEditingMovie(movie) }
                        )
                    }
                }
            }
        }
    }
}

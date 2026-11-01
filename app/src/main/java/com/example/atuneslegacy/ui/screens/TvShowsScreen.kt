package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import com.example.atuneslegacy.ui.components.TvBulkEditDialog
import com.example.atuneslegacy.ui.components.TvEpisodeRow
import com.example.atuneslegacy.ui.components.TvShowCard
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun TvShowsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val shows by viewModel.allTvShows.collectAsState()
    val selectedShow by viewModel.selectedTvShow.collectAsState()
    val showEpisodes by viewModel.tvEpisodesForSelectedShow.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        if (selectedShow != null) {
            // TV SHOW DETAIL PAGE (Episodes grouped by Season)
            TvShowDetailPage(
                show = selectedShow!!,
                episodes = showEpisodes,
                onBackClick = { viewModel.selectTvShow(null) },
                viewModel = viewModel
            )
        } else {
            // MAIN TV SHOWS LIST / GRID
            if (shows.isEmpty()) {
                EmptyMediaNotice("No TV Shows Available", "Import TV shows into your iTunes library.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(shows) { show ->
                        TvShowCard(
                            show = show,
                            onClick = { viewModel.selectTvShow(show) },
                            onLongClick = { viewModel.setEditingTvShow(show) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvShowDetailPage(
    show: TvShowEntity,
    episodes: List<TvEpisodeEntity>,
    onBackClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val isMultiSelectMode by viewModel.isTvMultiSelectMode.collectAsState()
    val selectedIds by viewModel.selectedTvEpisodeIds.collectAsState()
    val showBulkEdit by viewModel.showTvBulkEditDialog.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E6EE))
                .combinedClickable(onClick = {}, onLongClick = { viewModel.setEditingTvShow(show) })
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackClick) {
                Text("← All TV Shows", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = show.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                Text(
                    text = "Release: ${show.releaseYear} | Rating: ${show.parentalRating.ifBlank { "Unrated" }}",
                    fontSize = 12.sp,
                    color = Color(0xFF606674)
                )
            }
        }

        if (isMultiSelectMode) {
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC0C4CB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1D21)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                if (selectedIds.size == 1) {
                                    val singleId = selectedIds.first()
                                    val epToEdit = episodes.firstOrNull { it.id == singleId }
                                    if (epToEdit != null) {
                                        viewModel.clearTvEpisodeSelection()
                                        viewModel.setEditingTvEpisode(epToEdit)
                                    }
                                } else {
                                    viewModel.setShowTvBulkEditDialog(true)
                                }
                            },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (selectedIds.size <= 1) "Edit Metadata" else "Bulk Edit Tags", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.deleteSelectedTvEpisodes() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Delete", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearTvEpisodeSelection() },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (showBulkEdit) {
            TvBulkEditDialog(
                selectedCount = selectedIds.size,
                onDismiss = { viewModel.setShowTvBulkEditDialog(false) },
                onSave = { seasonStr, titlePrefix ->
                    viewModel.bulkUpdateTvEpisodesMetadata(seasonStr, titlePrefix)
                }
            )
        }

        if (episodes.isEmpty()) {
            EmptyMediaNotice("No Episodes Found", "No episode files added for this TV show yet.")
        } else {
            // Group episodes by season
            val groupedBySeason = episodes.groupBy { it.seasonNumber }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groupedBySeason.forEach { (seasonNum, seasonEpisodes) ->
                    item {
                        // Season Header Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFD6DADF))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Season $seasonNum",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C313C)
                            )
                        }
                    }
                    items(seasonEpisodes) { episode ->
                        TvEpisodeRow(
                            episode = episode,
                            isMultiSelectMode = isMultiSelectMode,
                            isChecked = selectedIds.contains(episode.id),
                            onCheckedChange = { viewModel.toggleTvEpisodeSelection(episode.id) },
                            onClick = {
                                if (isMultiSelectMode) viewModel.toggleTvEpisodeSelection(episode.id)
                                else viewModel.playTvEpisode(episode)
                            },
                            onLongClick = { viewModel.toggleTvEpisodeSelection(episode.id) }
                        )
                    }
                }
            }
        }
    }
}

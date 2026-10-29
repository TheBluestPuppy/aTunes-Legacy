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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
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
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.components.PodcastShowCard
import com.example.atuneslegacy.ui.navigation.PodcastSubTab
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun PodcastsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubTab by viewModel.podcastSubTab.collectAsState()
    val podcastShows by viewModel.allPodcastShows.collectAsState()
    val selectedPodcastShow by viewModel.selectedPodcastShow.collectAsState()
    val showEpisodes by viewModel.podcastEpisodesForSelectedShow.collectAsState()
    val selectedCategory by viewModel.selectedPodcastCategory.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        // Sub-Tab Switcher
        ITunesSegmentedControl(
            items = PodcastSubTab.entries,
            selectedItem = currentSubTab,
            onItemSelected = { viewModel.selectPodcastSubTab(it) },
            getItemLabel = { it.title }
        )

        if (selectedPodcastShow != null) {
            // PODCAST SHOW DETAIL PAGE (Episodes sorted numerically & grouped by Season headers)
            PodcastShowDetailPage(
                show = selectedPodcastShow!!,
                episodes = showEpisodes,
                onBackClick = { viewModel.selectPodcastShow(null) },
                viewModel = viewModel
            )
        } else {
            when (currentSubTab) {
                PodcastSubTab.SHOWS -> {
                    if (podcastShows.isEmpty()) {
                        EmptyMediaNotice("No Podcast Shows", "Subscribe or import podcast shows to your library.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 130.dp),
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(podcastShows) { show ->
                                PodcastShowCard(
                                    show = show,
                                    onClick = { viewModel.selectPodcastShow(show) },
                                    onLongClick = { viewModel.setEditingPodcastShow(show) }
                                )
                            }
                        }
                    }
                }
                PodcastSubTab.CATEGORIES -> {
                    PodcastCategoriesSubScreen(
                        podcastShows = podcastShows,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.selectPodcastCategory(it) },
                        onSelectShow = { viewModel.selectPodcastShow(it) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun PodcastShowDetailPage(
    show: PodcastShowEntity,
    episodes: List<PodcastEpisodeEntity>,
    onBackClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E6EE))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackClick) {
                Text("← All Podcasts", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = show.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                Text(text = "${show.creatorHost} • ${show.category}", fontSize = 12.sp, color = Color(0xFF606674))
            }
        }

        if (episodes.isEmpty()) {
            EmptyMediaNotice("No Episodes Found", "No episodes retrieved for this podcast.")
        } else {
            val groupedBySeason = episodes.groupBy { it.seasonNumber }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groupedBySeason.forEach { (seasonNum, seasonEpisodes) ->
                    item {
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        viewModel.playPodcastEpisode(episode)
                                    },
                                    onLongClick = {
                                        viewModel.setEditingPodcastEpisode(episode)
                                    }
                                )
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = ITunesAccentBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = episode.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1D21)
                                )
                                Text(
                                    text = "Episode ${episode.episodeNumber} (Season ${episode.seasonNumber})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF687080)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PodcastCategoriesSubScreen(
    podcastShows: List<PodcastShowEntity>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    onSelectShow: (PodcastShowEntity) -> Unit,
    viewModel: MainViewModel
) {
    val categories = podcastShows.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()

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
                        .clickable { onSelectCategory(null) }
                        .background(if (selectedCategory == null) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "All Categories (${categories.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategory == null) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFD0D4DD))
            }
            items(categories) { category ->
                val isSelected = category.equals(selectedCategory, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCategory(category) }
                        .background(if (isSelected) ITunesAccentBlue else Color.Transparent)
                        .padding(10.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF2C3038)
                    )
                }
                HorizontalDivider(color = Color(0xFFE0E3EA))
            }
        }

        val filteredShows = if (selectedCategory == null) podcastShows else podcastShows.filter { it.category.equals(selectedCategory, ignoreCase = true) }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.weight(1f).fillMaxSize()
        ) {
            items(filteredShows) { show ->
                PodcastShowCard(
                    show = show,
                    onClick = { onSelectShow(show) },
                    onLongClick = { viewModel.setEditingPodcastShow(show) }
                )
            }
        }
    }
}

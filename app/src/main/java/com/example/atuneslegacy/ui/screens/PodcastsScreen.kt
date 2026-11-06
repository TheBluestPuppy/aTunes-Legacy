package com.example.atuneslegacy.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.example.atuneslegacy.util.openMediaExternally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.R
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

    Box(modifier = modifier.fillMaxSize()) {
        // iOS 6 Custom Background Image
        AsyncImage(
            model = R.drawable.ios6_background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Sub-Tab Switcher
            ITunesSegmentedControl(
                items = PodcastSubTab.entries,
                selectedItem = currentSubTab,
                onItemSelected = { viewModel.selectPodcastSubTab(it) },
                getItemLabel = { it.title }
            )

            if (selectedPodcastShow != null) {
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
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(podcastShows) { show ->
                                    PodcastShowCard(
                                        show = show,
                                        onClick = { viewModel.selectPodcastShow(show) },
                                        onLongClick = { viewModel.setEditingPodcastShow(show) },
                                        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth()
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
}

@Composable
private fun PodcastShowDetailPage(
    show: PodcastShowEntity,
    episodes: List<PodcastEpisodeEntity>,
    onBackClick: () -> Unit,
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = R.drawable.ios6_background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Nav Button Bar (+ All Podcasts >)
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFA2A7B5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBackClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("+ ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3038))
                            Text("All Podcasts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Show Header Card
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFA2A7B5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE2E6EE))
                                    .border(1.dp, Color(0xFFC0C5CE), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (show.logoThumbnailUri.isNotBlank()) {
                                    AsyncImage(
                                        model = show.logoThumbnailUri,
                                        contentDescription = show.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Podcasts,
                                        contentDescription = null,
                                        tint = Color(0xFF9098A8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = show.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Host: ${show.creatorHost.ifBlank { "Quin Halvorson" }} | Category: ${show.category.ifBlank { "Video Games" }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF555D6E)
                                )
                            }
                        }

                        val descText = show.description.ifBlank {
                            "We're the podcast that jumps deep into the lore of the Half-Life/Portal universe.\nWhether you're fighting Combine or solving test chambers,"
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = descText,
                            fontSize = 12.sp,
                            color = Color(0xFF424855),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Season Section Header & Episodes Table
            val groupedBySeason = episodes.groupBy { it.seasonNumber }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                groupedBySeason.forEach { (seasonNum, seasonEpisodes) ->
                    item {
                        // iOS 6 Glossy Section Header Bar
                        Surface(
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                            color = Color.Transparent,
                            border = BorderStroke(0.5.dp, Color(0xFF959AA6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(Color(0xFFBAC0CC), Color(0xFF9EA3AF))
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Season $seasonNum",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F232C)
                                )
                            }
                        }
                    }

                    items(seasonEpisodes) { episode ->
                        Surface(
                            color = Color.White,
                            border = BorderStroke(0.5.dp, Color(0xFFE2E5EC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { viewModel.playPodcastEpisode(episode) },
                                    onLongClick = { viewModel.setEditingPodcastEpisode(episode) }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Play",
                                    tint = Color(0xFF2B73E7),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Episode ${episode.episodeNumber}: ${episode.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Podcast: ${episode.podcastName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF656C7B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF8E94A2),
                                    modifier = Modifier.size(20.dp)
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
private fun PodcastCategoriesSubScreen(
    podcastShows: List<PodcastShowEntity>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    onSelectShow: (PodcastShowEntity) -> Unit,
    viewModel: MainViewModel
) {
    val categories = podcastShows.map { it.category.ifBlank { "Podcast" } }.distinct().sorted()

    if (selectedCategory != null) {
        val catShows = podcastShows.filter { (it.category.ifBlank { "Podcast" }) == selectedCategory }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E6EE))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { onSelectCategory(null) }) {
                    Text("← All Categories", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = selectedCategory, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
            }
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(catShows) { show ->
                    PodcastShowCard(
                        show = show,
                        onClick = { onSelectShow(show) },
                        onLongClick = { viewModel.setEditingPodcastShow(show) },
                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                    )
                }
            }
        }
    } else {
        if (categories.isEmpty()) {
            EmptyMediaNotice("No Categories", "Add podcast shows to view categories.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clickable { onSelectCategory(cat) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1D21)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
                }
            }
        }
    }
}

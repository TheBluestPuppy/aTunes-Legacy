package com.example.atuneslegacy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

data class MilestoneItem(
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val progress: String
)

@Composable
fun MilestonesScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val songs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()

    val totalSongs = songs.size
    val genres = songs.map { it.genre.lowercase() }.toSet()

    val milestones = listOf(
        MilestoneItem("First Steps", "Play your very first song on the app.", totalSongs > 0, if (totalSongs > 0) "1/1" else "0/1"),
        MilestoneItem("On Repeat", "Play the same track 50 times in a single week.", false, "0/50"),
        MilestoneItem("Night Owl", "Listen to music between 11:00 pm and 2:00 am.", false, "Locked"),
        MilestoneItem("Early Bird", "Listen to music between 3:00 am and 5:00 am.", false, "Locked"),
        MilestoneItem("Marathon Runner", "Listen to music continuously for 8 hours straight.", false, "0h / 8h"),
        MilestoneItem("Streak Keeper", "Maintain a daily listening streak for 30 days.", false, "0/30 days"),
        MilestoneItem("Dedicated", "Save 100 songs to your library.", totalSongs >= 100, "$totalSongs/100"),
        MilestoneItem("Enthusiast", "Save 500 songs to your library.", totalSongs >= 500, "$totalSongs/500"),
        MilestoneItem("Hoarder", "Save 1000 songs to your library.", totalSongs >= 1000, "$totalSongs/1000"),
        MilestoneItem("Database Legend", "Index over 10,000 unique tracks in the app library.", totalSongs >= 10000, "$totalSongs/10000"),
        MilestoneItem("Album Purist", "Listen to a full album from start to finish without skipping (at least 5 songs).", false, "0/1"),
        MilestoneItem("Genre Explorer", "Have at least one song from each genre: Pop, Hip-Hop, Rock, Country, Latin, Jazz, Rap.",
            genres.contains("pop") && genres.contains("hip-hop") && genres.contains("rock") && genres.contains("country") && genres.contains("latin") && genres.contains("jazz") && genres.contains("rap"),
            "Genres found: ${listOf("pop", "hip-hop", "rock", "country", "latin", "jazz", "rap").count { genres.contains(it) }}/7"),
        MilestoneItem("Curator", "Create your first playlist.", playlists.isNotEmpty(), if (playlists.isNotEmpty()) "1/1" else "0/1"),
        MilestoneItem("Mixtape Master", "Build a playlist that crosses 3 or more wildly different genres.", false, "0/1"),
        MilestoneItem("Mixtape Craftsman", "Manually build a custom playlist containing over 100 tracks.", false, "0/100"),
        MilestoneItem("Interior Designer", "Change the app's theme for the first time.", true, "1/1"),
        MilestoneItem("Cover Flow Nostalgia", "Spend a continuous 5 minutes browsing through the cover flow.", false, "0/5 mins")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Top Bar
        Surface(
            color = themeColors.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBackClick) {
                    Text("← Profile", fontSize = 12.sp, color = themeColors.textPrimary)
                }
                Text(
                    text = "Milestones & Achievements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.width(60.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(milestones) { item ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = themeColors.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (item.isCompleted) Color(0xFF10B981) else Color(0xFF94A3B8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = themeColors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Progress: ${item.progress}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCompleted) Color(0xFF10B981) else themeColors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (item.isCompleted) Color(0xFF10B981) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

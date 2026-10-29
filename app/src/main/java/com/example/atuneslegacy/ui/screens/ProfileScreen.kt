package com.example.atuneslegacy.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    val songs by viewModel.allSongs.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editBio by remember { mutableStateOf(userBio) }

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setProfilePictureUri(it.toString()) }
    }

    val artistsCount = songs.map { it.artist }.distinct().size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (themeColors.isDark) themeColors.background else Color(0xFFD8DCE5))
            .verticalScroll(rememberScrollState())
    ) {
        // Classic iOS Header Bar with Edit Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (themeColors.isDark) {
                            listOf(Color(0xFF32363C), Color(0xFF1E2125))
                        } else {
                            listOf(Color(0xFF9EBCD0), Color(0xFF5B82A8))
                        }
                    )
                )
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("← Settings", fontSize = 11.sp)
                }
                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = {
                        editName = userName
                        editBio = userBio
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Edit", fontSize = 11.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. User Identity Card
            IOSProfileGroupCard(themeColors = themeColors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editName = userName
                            editBio = userBio
                            showEditDialog = true
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColors.background)
                            .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePictureUri != null) {
                            AsyncImage(
                                model = profilePictureUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userBio,
                            fontSize = 12.sp,
                            color = themeColors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Offline Storage & Usage Stats
            Text(
                text = "Storage",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (themeColors.isDark) Color.LightGray else Color(0xFF4A5568),
                modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
            )
            IOSProfileGroupCard(themeColors = themeColors) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF3B82F6)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text("Local Storage", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.22f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = themeColors.primaryAccent,
                        trackColor = themeColors.surfaceBorder,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "14.2 GB used of 64 GB available",
                        fontSize = 11.sp,
                        color = themeColors.textSecondary
                    )
                }
                IOSProfileDivider(themeColors = themeColors)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Manage storage action */ }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF64748B)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text("Manage Downloaded Files", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                            Text("Clear cache, remove downloads, etc.", fontSize = 11.sp, color = themeColors.textSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            // 3. Listening Insights
            Text(
                text = "Listening Insights",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (themeColors.isDark) Color.LightGray else Color(0xFF4A5568),
                modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
            )
            IOSProfileGroupCard(themeColors = themeColors) {
                ProfileInsightCell(
                    icon = Icons.Default.MusicNote,
                    iconBg = Color(0xFF10B981),
                    title = "Top Tracks",
                    value = songs.firstOrNull()?.let { "1. ${it.title} - ${it.artist} (Local)" } ?: "No tracks played"
                )
                IOSProfileDivider(themeColors = themeColors)
                ProfileInsightCell(
                    icon = Icons.Default.Album,
                    iconBg = Color(0xFF8B5CF6),
                    title = "Top Albums",
                    value = albums.firstOrNull()?.let { "1. ${it.title} - ${it.albumArtist} (Local)" } ?: "No albums found"
                )
                IOSProfileDivider(themeColors = themeColors)
                ProfileInsightCell(
                    icon = Icons.Default.Person,
                    iconBg = Color(0xFFF59E0B),
                    title = "Top Artists",
                    value = songs.map { it.artist }.distinct().take(3).joinToString("   ") { it }
                        .ifBlank { "No artists found" }
                )
                IOSProfileDivider(themeColors = themeColors)
                ProfileInsightCell(
                    icon = Icons.Default.Schedule,
                    iconBg = Color(0xFF3B82F6),
                    title = "Total Listening Time",
                    value = "48.7 hours"
                )
                IOSProfileDivider(themeColors = themeColors)
                ProfileInsightCell(
                    icon = Icons.Default.EmojiEvents,
                    iconBg = Color(0xFFEAB308),
                    title = "Milestones",
                    value = "100 hours of local playback  •  Album Completionist"
                )
            }

            // 4. Library Summary
            Text(
                text = "Library Summary",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (themeColors.isDark) Color.LightGray else Color(0xFF4A505C),
                modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
            )
            IOSProfileGroupCard(themeColors = themeColors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LibraryStatColumn(label = "Tracks", count = songs.size.toString(), themeColors = themeColors)
                    LibraryStatDivider(themeColors = themeColors)
                    LibraryStatColumn(label = "Albums", count = albums.size.toString(), themeColors = themeColors)
                    LibraryStatDivider(themeColors = themeColors)
                    LibraryStatColumn(label = "Artists", count = artistsCount.toString(), themeColors = themeColors)
                    LibraryStatDivider(themeColors = themeColors)
                    LibraryStatColumn(label = "Playlists", count = playlists.size.toString(), themeColors = themeColors)
                }
            }

            // 5. Recently Added
            Text(
                text = "Recently Added",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (themeColors.isDark) Color.LightGray else Color(0xFF4A505C),
                modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
            )
            IOSProfileGroupCard(themeColors = themeColors) {
                if (songs.isEmpty()) {
                    Text(
                        text = "No recent imports.",
                        fontSize = 12.sp,
                        color = themeColors.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    songs.take(3).forEachIndexed { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(themeColors.background),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (song.artworkUri.isNotBlank()) {
                                        AsyncImage(model = song.artworkUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = themeColors.primaryAccent, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = song.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist}  •  MP3  •  Added ${index + 1} hours ago",
                                        fontSize = 10.sp,
                                        color = themeColors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        if (index < 2 && index < songs.size - 1) {
                            IOSProfileDivider(themeColors = themeColors)
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = themeColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { profileImagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Upload Profile Photo / Avatar")
                    }
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / Status") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(editName, editBio)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun IOSProfileGroupCard(
    themeColors: com.example.atuneslegacy.ui.theme.ThemeColors,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = themeColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.surfaceBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun ProfileInsightCell(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    value: String
) {
    val themeColors = LocalThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    color = themeColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun LibraryStatColumn(label: String, count: String, themeColors: com.example.atuneslegacy.ui.theme.ThemeColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = themeColors.textSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
    }
}

@Composable
fun LibraryStatDivider(themeColors: com.example.atuneslegacy.ui.theme.ThemeColors) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(0.5.dp)
            .background(themeColors.surfaceBorder)
    )
}

@Composable
fun IOSProfileDivider(themeColors: com.example.atuneslegacy.ui.theme.ThemeColors) {
    HorizontalDivider(
        color = themeColors.surfaceBorder,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 50.dp)
    )
}

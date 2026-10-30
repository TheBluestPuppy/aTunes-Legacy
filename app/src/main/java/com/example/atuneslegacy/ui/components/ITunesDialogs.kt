package com.example.atuneslegacy.ui.components

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.data.importing.ImportCategory
import com.example.atuneslegacy.ui.theme.LocalThemeColors

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Playlist",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a name for your new playlist:",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = { Text("Untitled Playlist", color = themeColors.textSecondary) },
                    singleLine = true,
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onCreate(playlistName.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = themeColors.surface
    )
}

@Composable
fun AddMediaDialog(
    onDismiss: () -> Unit,
    onImportFiles: (uris: List<Uri>, category: ImportCategory) -> Unit,
    onSelectGamesApps: (packageNames: List<String>) -> Unit = {}
) {
    val themeColors = LocalThemeColors.current
    var selectedCategory by remember { mutableStateOf(ImportCategory.MUSIC) }
    var showSelectAppsDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportFiles(uris, selectedCategory)
            onDismiss()
        }
    }

    fun launchPickerFor(category: ImportCategory) {
        if (category == ImportCategory.GAMES) {
            showSelectAppsDialog = true
            return
        }
        selectedCategory = category
        val mimeTypes = when (category) {
            ImportCategory.MUSIC -> arrayOf("audio/*", "video/*")
            ImportCategory.TV_SHOWS -> arrayOf("video/*")
            ImportCategory.MOVIES -> arrayOf("video/*")
            ImportCategory.PODCASTS -> arrayOf("audio/*")
            ImportCategory.AUDIOBOOKS -> arrayOf("audio/*", "application/pdf", "application/epub+zip")
            ImportCategory.GAMES -> arrayOf("*/*")
        }
        try {
            filePickerLauncher.launch(mimeTypes)
        } catch (e: Exception) {
            filePickerLauncher.launch(arrayOf("*/*"))
        }
    }

    if (showSelectAppsDialog) {
        SelectAppsDialog(
            onDismiss = {
                showSelectAppsDialog = false
                onDismiss()
            },
            onAddApps = { packageNames ->
                onSelectGamesApps(packageNames)
                showSelectAppsDialog = false
                onDismiss()
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Add Media to Library",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select a media category below to import local files into your database:",
                        fontSize = 12.sp,
                        color = themeColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImportCategoryOptionButton(
                            title = "Music (Audio & Music Videos)",
                            icon = Icons.Default.MusicNote,
                            onClick = { launchPickerFor(ImportCategory.MUSIC) }
                        )
                        ImportCategoryOptionButton(
                            title = "TV-Shows (Video Episodes)",
                            icon = Icons.Default.Tv,
                            onClick = { launchPickerFor(ImportCategory.TV_SHOWS) }
                        )
                        ImportCategoryOptionButton(
                            title = "Movies (Full Movies)",
                            icon = Icons.Default.Movie,
                            onClick = { launchPickerFor(ImportCategory.MOVIES) }
                        )
                        ImportCategoryOptionButton(
                            title = "Podcasts (Podcast Episodes)",
                            icon = Icons.Default.Podcasts,
                            onClick = { launchPickerFor(ImportCategory.PODCASTS) }
                        )
                        ImportCategoryOptionButton(
                            title = "Audiobooks (Books & Audio)",
                            icon = Icons.Default.Book,
                            onClick = { launchPickerFor(ImportCategory.AUDIOBOOKS) }
                        )
                        ImportCategoryOptionButton(
                            title = "Games (Apps)",
                            icon = Icons.Default.SportsEsports,
                            onClick = { launchPickerFor(ImportCategory.GAMES) }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun SelectAppsDialog(
    onDismiss: () -> Unit,
    onAddApps: (List<String>) -> Unit
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current

    val installedApps = remember {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.filter { app ->
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }.sortedBy { it.loadLabel(pm).toString() }
    }

    var selectedPackages by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Games & Apps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Check the apps you want to add to your aTunes Games tab:",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(installedApps) { app ->
                        val pm = context.packageManager
                        val appName = app.loadLabel(pm).toString()
                        val appIcon = app.loadIcon(pm)
                        val isChecked = selectedPackages.contains(app.packageName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPackages = if (isChecked) {
                                        selectedPackages - app.packageName
                                    } else {
                                        selectedPackages + app.packageName
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedPackages = if (checked) {
                                        selectedPackages + app.packageName
                                    } else {
                                        selectedPackages - app.packageName
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AsyncImage(
                                model = appIcon,
                                contentDescription = appName,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = appName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPackages.isNotEmpty()) {
                        onAddApps(selectedPackages.toList())
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent)
            ) {
                Text("Add Selected (${selectedPackages.size})")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = themeColors.surface
    )
}

@Composable
private fun ImportCategoryOptionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val themeColors = LocalThemeColors.current

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = themeColors.textPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.surfaceBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.primaryAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

package com.example.atuneslegacy.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onImportFiles: (uris: List<Uri>, category: ImportCategory) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var selectedCategory by remember { mutableStateOf(ImportCategory.MUSIC) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportFiles(uris, selectedCategory)
            onDismiss()
        }
    }

    fun launchPickerFor(category: ImportCategory) {
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

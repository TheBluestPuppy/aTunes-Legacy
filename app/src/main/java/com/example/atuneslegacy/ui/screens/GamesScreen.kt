package com.example.atuneslegacy.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val removedPackages by viewModel.removedGamePackages.collectAsState()
    var appToRemove by remember { mutableStateOf<ApplicationInfo?>(null) }

    val installedApps = remember(removedPackages) {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.filter { app ->
            !removedPackages.contains(app.packageName) &&
                    ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
        }.sortedBy { it.loadLabel(pm).toString() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        if (installedApps.isEmpty()) {
            EmptyMediaNotice(
                title = "No Games / Apps Found",
                subtitle = "Import or add apps via Settings > Add Media to Library."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(installedApps) { app ->
                    val pm = context.packageManager
                    val appName = app.loadLabel(pm).toString()
                    val appIcon = app.loadIcon(pm)

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    }
                                },
                                onLongClick = {
                                    appToRemove = app
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = appIcon,
                                contentDescription = appName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = appName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    appToRemove?.let { app ->
        val pm = context.packageManager
        val appName = app.loadLabel(pm).toString()
        AlertDialog(
            onDismissRequest = { appToRemove = null },
            title = { Text("Remove Shortcut?", fontWeight = FontWeight.Bold, color = themeColors.textPrimary) },
            text = { Text("Do you want to remove '$appName' shortcut from your aTunes Games tab?", color = themeColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeGameShortcut(app.packageName)
                        appToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { appToRemove = null }) {
                    Text("Cancel")
                }
            },
            containerColor = themeColors.surface
        )
    }
}

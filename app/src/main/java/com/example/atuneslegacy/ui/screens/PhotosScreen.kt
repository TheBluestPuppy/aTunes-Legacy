package com.example.atuneslegacy.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun PhotosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current

    val photoFiles = remember {
        val dcimCamera = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
        if (dcimCamera.exists() && dcimCamera.isDirectory) {
            val list = dcimCamera.listFiles { file ->
                file.isFile && (file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) || file.name.endsWith(".jpeg", true))
            }
            list?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList<File>()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        if (photoFiles.isEmpty()) {
            EmptyMediaNotice(
                title = "No Camera Photos Found",
                subtitle = "Photos stored in DCIM/Camera will appear here automatically."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photoFiles) { file ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(themeColors.surface)
                            .border(1.dp, themeColors.surfaceBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = file.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

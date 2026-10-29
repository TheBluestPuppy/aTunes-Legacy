package com.example.atuneslegacy.ui.screens

import android.media.MediaRecorder
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentFile by remember { mutableStateOf<File?>(null) }

    val recordingsDir = remember {
        val dir = File(Environment.getExternalStorageDirectory(), "Recordings/aTunes")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    var refreshTrigger by remember { mutableStateOf(0) }
    val recordingFiles = remember(refreshTrigger) {
        if (recordingsDir.exists() && recordingsDir.isDirectory) {
            recordingsDir.listFiles { file -> file.isFile && (file.name.endsWith(".3gp", true) || file.name.endsWith(".m4a", true)) }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT) }
    val displayDateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT) }

    fun startRecording() {
        try {
            val timestamp = dateFormat.format(Date())
            val file = File(recordingsDir, "Voice_$timestamp.m4a")
            currentFile = file

            @Suppress("DEPRECATION")
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            isPaused = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseRecording() {
        try {
            if (isRecording) {
                if (isPaused) {
                    mediaRecorder?.resume()
                    isPaused = false
                } else {
                    mediaRecorder?.pause()
                    isPaused = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            isPaused = false
            refreshTrigger++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Recorder Controls Header
        Surface(
            color = themeColors.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, themeColors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) (if (isPaused) "Recording Paused..." else "Recording Voice...") else "Voice Recorder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording) {
                        Button(
                            onClick = { startRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { pauseRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPaused) "Resume" else "Pause", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { stopRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop & Save", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = themeColors.surfaceBorder, thickness = 0.5.dp)

        // Recordings List
        if (recordingFiles.isEmpty()) {
            EmptyMediaNotice(
                title = "No Voice Recordings",
                subtitle = "Recordings saved to Internal Storage > Recordings > aTunes will appear here."
            )
        } else {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(recordingFiles) { index, file ->
                    val songEntity = SongEntity(
                        id = file.absolutePath.hashCode().toLong(),
                        title = file.name,
                        artist = "Voice Recorder",
                        albumArtist = "aTunes",
                        albumTitle = "Recordings",
                        genre = "Voice",
                        year = currentYear,
                        trackNumber = index + 1,
                        uri = android.net.Uri.fromFile(file).toString()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColors.surface, RoundedCornerShape(6.dp))
                            .border(0.5.dp, themeColors.surfaceBorder, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { viewModel.playSong(songEntity, recordingFiles.map { f ->
                                    SongEntity(
                                        id = f.absolutePath.hashCode().toLong(),
                                        title = f.name,
                                        artist = "Voice Recorder",
                                        albumArtist = "aTunes",
                                        albumTitle = "Recordings",
                                        genre = "Voice",
                                        year = currentYear,
                                        uri = android.net.Uri.fromFile(f).toString()
                                    )
                                }) },
                                modifier = Modifier
                                    .background(themeColors.primaryAccent, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = file.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.textPrimary
                                )
                                Text(
                                    text = "${file.length() / 1024} KB • ${displayDateFormat.format(Date(file.lastModified()))}",
                                    fontSize = 11.sp,
                                    color = themeColors.textSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

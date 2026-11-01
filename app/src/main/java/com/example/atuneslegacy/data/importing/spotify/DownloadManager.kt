package com.example.atuneslegacy.data.importing.spotify

import android.content.Context
import android.net.Uri
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.remote.KtorHttpClient
import com.example.atuneslegacy.data.repository.MediaRepository
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.FileOutputStream

object DownloadManager {
    private val client: HttpClient = KtorHttpClient.client

    suspend fun download(
        context: Context,
        originalMeta: UnifiedTrackMetadata,
        candidate: CandidateRecording,
        mediaRepository: MediaRepository,
        onProgress: (Float, String) -> Unit
    ): Boolean {
        try {
            onProgress(0.1f, "Connecting to download source...")
            val httpResponse = client.get(candidate.downloadUrl)
            val channel = httpResponse.bodyAsChannel()
            val totalLength = httpResponse.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: (candidate.fileSize.takeIf { it > 0 } ?: 4500000L)
            var downloaded: Long = 0

            val musicDir = File(context.filesDir, "DownloadedMusic")
            if (!musicDir.exists()) musicDir.mkdirs()

            val sanitizedTitle = originalMeta.title.replace(Regex("[^A-Za-z0-9 ]"), "").replace(" ", "_")
            val tempFile = File(musicDir, "temp_${sanitizedTitle}_${System.currentTimeMillis()}.tmp")
            val finalFile = File(musicDir, "${sanitizedTitle}_${System.currentTimeMillis()}.mp3")

            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(8192)
                while (!channel.isClosedForRead) {
                    val packet = channel.readAvailable(buffer, 0, buffer.size)
                    if (packet <= 0) break
                    output.write(buffer, 0, packet)
                    downloaded += packet
                    val progress = (downloaded.toFloat() / totalLength.toFloat()).coerceIn(0.1f, 0.95f)
                    onProgress(progress, "Downloading ${(downloaded / 1024)} KB...")
                }
            }

            if (!tempFile.exists() || tempFile.length() <= 0L) {
                if (tempFile.exists()) tempFile.delete()
                onProgress(0f, "Download failed: File is corrupted or empty.")
                return false
            }

            tempFile.renameTo(finalFile)

            onProgress(0.98f, "Adding to music library...")

            val localUri = Uri.fromFile(finalFile).toString()
            val song = SongEntity(
                title = originalMeta.title,
                artist = originalMeta.artist,
                albumArtist = originalMeta.artist,
                albumTitle = originalMeta.albumName,
                genre = "Spotify Download",
                year = originalMeta.releaseDate.toIntOrNull() ?: 2024,
                trackNumber = originalMeta.trackNumber,
                filePath = finalFile.absolutePath,
                uri = localUri,
                duration = originalMeta.durationMs,
                artworkUri = originalMeta.artworkUrl,
                isMusicVideo = false
            )

            mediaRepository.insertSong(song)

            val album = AlbumEntity(
                title = originalMeta.albumName,
                albumArtist = originalMeta.artist,
                genre = "Spotify Download",
                releaseYear = originalMeta.releaseDate.toIntOrNull() ?: 2024,
                albumCoverUri = originalMeta.artworkUrl
            )
            mediaRepository.insertAlbum(album)

            onProgress(1.0f, "Download complete!")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(0f, "Download failed: ${e.localizedMessage}")
            return false
        }
    }
}

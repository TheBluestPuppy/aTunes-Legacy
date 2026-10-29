package com.example.atuneslegacy.data.importing

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import java.io.File
import java.io.FileOutputStream

enum class ImportCategory {
    MUSIC, TV_SHOWS, MOVIES, PODCASTS, AUDIOBOOKS, GAMES
}

data class ExtractedMediaMetadata(
    val title: String,
    val artistOrCreator: String = "",
    val albumOrShowOrSeries: String = "",
    val genre: String = "",
    val releaseYear: Int = 0,
    val trackNumber: Int = 1,
    val durationMs: Long = 0L,
    val isVideo: Boolean = false,
    val artworkUri: String = ""
)

object MediaImportHelper {

    fun takePersistablePermission(context: Context, uri: Uri) {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission might not be persistable or already granted
            }
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to path segment
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1 && result != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Untitled"
    }

    fun cleanTitleFromFilename(filename: String): String {
        val extensions = listOf(".mp3", ".mp4", ".m4a", ".mkv", ".avi", ".flac", ".wav", ".aac", ".pdf", ".epub")
        var clean = filename
        for (ext in extensions) {
            if (clean.endsWith(ext, ignoreCase = true)) {
                clean = clean.substring(0, clean.length - ext.length)
                break
            }
        }
        return clean.ifBlank { filename }
    }

    fun extractMetadata(context: Context, uri: Uri): ExtractedMediaMetadata {
        val fileName = getFileName(context, uri)
        val cleanName = cleanTitleFromFilename(fileName)

        var embeddedTitle: String? = null
        var embeddedArtist: String? = null
        var embeddedAlbum: String? = null
        var embeddedGenre: String? = null
        var embeddedYearStr: String? = null
        var embeddedTrackStr: String? = null
        var embeddedDurationStr: String? = null
        var isVideo = false
        var artworkUri = ""

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            embeddedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            embeddedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            embeddedAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            embeddedGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            embeddedYearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            embeddedTrackStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
            embeddedDurationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            val hasVideoStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            isVideo = hasVideoStr != null && hasVideoStr.equals("yes", ignoreCase = true)

            // Extract embedded picture if available
            val picture = retriever.embeddedPicture
            if (picture != null && picture.isNotEmpty()) {
                val imageFile = File(context.cacheDir, "cover_${System.currentTimeMillis()}.jpg")
                FileOutputStream(imageFile).use { out ->
                    out.write(picture)
                }
                artworkUri = Uri.fromFile(imageFile).toString()
            }
        } catch (e: Exception) {
            // Failed to retrieve metadata from retriever
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) { }
        }

        val title = embeddedTitle?.ifBlank { null } ?: cleanName
        val artist = embeddedArtist?.ifBlank { null } ?: ""
        val album = embeddedAlbum?.ifBlank { null } ?: ""
        val genre = embeddedGenre?.ifBlank { null } ?: ""
        val year = embeddedYearStr?.toIntOrNull() ?: 0
        val track = embeddedTrackStr?.split("/")?.firstOrNull()?.toIntOrNull() ?: 1
        val duration = embeddedDurationStr?.toLongOrNull() ?: 0L

        val mimeType = context.contentResolver.getType(uri) ?: ""
        if (mimeType.startsWith("video/")) {
            isVideo = true
        }

        return ExtractedMediaMetadata(
            title = title,
            artistOrCreator = artist,
            albumOrShowOrSeries = album,
            genre = genre,
            releaseYear = year,
            trackNumber = track,
            durationMs = duration,
            isVideo = isVideo,
            artworkUri = artworkUri
        )
    }

    // Process imported URI into SongEntity
    fun createSongEntity(context: Context, uri: Uri): SongEntity {
        takePersistablePermission(context, uri)
        val extracted = extractMetadata(context, uri)
        val fileName = getFileName(context, uri)
        val title = extracted.title.ifBlank { cleanTitleFromFilename(fileName) }

        return SongEntity(
            title = title,
            artist = extracted.artistOrCreator.ifBlank { "Unknown Artist" },
            albumArtist = extracted.artistOrCreator.ifBlank { "Unknown Artist" },
            albumTitle = extracted.albumOrShowOrSeries.ifBlank { "Unknown Album" },
            composer = "",
            genre = extracted.genre.ifBlank { "Unspecified" },
            year = if (extracted.releaseYear > 0) extracted.releaseYear else 2024,
            trackNumber = extracted.trackNumber,
            filePath = uri.toString(),
            uri = uri.toString(),
            duration = extracted.durationMs,
            artworkUri = extracted.artworkUri,
            isMusicVideo = extracted.isVideo
        )
    }

    // Process imported URI into TvEpisodeEntity
    fun createTvEpisodeEntity(context: Context, uri: Uri): TvEpisodeEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)

        return TvEpisodeEntity(
            title = title,
            showTitle = "Unspecified",
            seasonNumber = 0,
            episodeNumber = 0,
            videoPath = uri.toString(),
            uri = uri.toString(),
            thumbnailUri = ""
        )
    }

    // Process imported URI into MovieEntity
    fun createMovieEntity(context: Context, uri: Uri): MovieEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)

        return MovieEntity(
            title = title,
            artists = "",
            releaseYear = 0,
            parentalRating = "",
            thumbnailUri = "",
            videoPath = uri.toString(),
            uri = uri.toString()
        )
    }

    // Process imported URI into PodcastEpisodeEntity
    fun createPodcastEpisodeEntity(context: Context, uri: Uri): PodcastEpisodeEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)

        return PodcastEpisodeEntity(
            title = title,
            podcastName = "Unspecified",
            seasonNumber = 0,
            episodeNumber = 0,
            audioPath = uri.toString(),
            uri = uri.toString()
        )
    }

    // Process imported URI into AudiobookEntity
    fun createAudiobookEntity(context: Context, uri: Uri): AudiobookEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)

        return AudiobookEntity(
            title = title,
            author = "",
            bookSeries = "Unspecified",
            bookNumberInSeries = 0,
            genre = "",
            publicationYear = 0,
            portraitCoverUri = "",
            audioPath = uri.toString(),
            uri = uri.toString()
        )
    }
}

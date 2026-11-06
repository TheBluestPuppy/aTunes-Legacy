package com.example.atuneslegacy.data.importing

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.ShortVideoEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import com.example.atuneslegacy.data.local.entity.AudiobookChapter
import com.example.atuneslegacy.data.repository.MediaRepository
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

data class ParsedMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val description: String? = null,
    val starRating: Int? = null,
    val parentalRating: String? = null,
    val series: String? = null
)

data class ParsedSeasonEpisode(
    val fileName: String,
    val title: String,
    val episodeNumber: Int
)

fun parseSeasonMetadataTxt(txtFile: File): Pair<Int?, List<ParsedSeasonEpisode>> {
    var seasonNum: Int? = null
    val episodes = mutableListOf<ParsedSeasonEpisode>()

    try {
        if (txtFile.exists()) {
            val lines = txtFile.readLines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.lowercase().startsWith("season:")) {
                    seasonNum = trimmed.substringAfter(":").trim().toIntOrNull()
                } else if (trimmed.startsWith("-")) {
                    try {
                        val content = trimmed.removePrefix("-").trim()
                        val quoteStart = content.indexOfAny(charArrayOf('"', '“', '\''))
                        val quoteEnd = content.lastIndexOfAny(charArrayOf('"', '”', '\''))
                        if (quoteStart != -1 && quoteEnd > quoteStart) {
                            val rawFileName = content.substring(0, quoteStart).trim().removeSuffix("(").trim()
                            val title = content.substring(quoteStart + 1, quoteEnd).trim()
                            val afterQuote = content.substring(quoteEnd + 1).trim().removeSuffix(")")
                            val epNum = afterQuote.filter { it.isDigit() }.toIntOrNull()
                                ?: rawFileName.filter { it.isDigit() }.toIntOrNull() ?: 1

                            episodes.add(ParsedSeasonEpisode(rawFileName, title, epNum))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return Pair(seasonNum, episodes)
}

fun parseMetadataTxt(txtFile: File): ParsedMetadata {
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var genre: String? = null
    var year: Int? = null
    var description: String? = null
    var starRating: Int? = null
    var parentalRating: String? = null
    var series: String? = null

    try {
        if (txtFile.exists()) {
            val lines = txtFile.readLines()
            var currentKey: String? = null
            var currentValue = StringBuilder()

            for (line in lines) {
                val trimmed = line.trim()
                val colonIdx = trimmed.indexOf(":")
                val isNewKey = colonIdx != -1 && (
                    trimmed.startsWith("Show Title:", ignoreCase = true) ||
                    trimmed.startsWith("Title:", ignoreCase = true) ||
                    trimmed.startsWith("Host:", ignoreCase = true) ||
                    trimmed.startsWith("Author:", ignoreCase = true) ||
                    trimmed.startsWith("Artist:", ignoreCase = true) ||
                    trimmed.startsWith("Rating:", ignoreCase = true) ||
                    trimmed.startsWith("Catagory:", ignoreCase = true) ||
                    trimmed.startsWith("Category:", ignoreCase = true) ||
                    trimmed.startsWith("Description:", ignoreCase = true) ||
                    trimmed.startsWith("Show Description:", ignoreCase = true) ||
                    trimmed.startsWith("Parental Rating:", ignoreCase = true) ||
                    trimmed.startsWith("Release Year:", ignoreCase = true) ||
                    trimmed.startsWith("Year:", ignoreCase = true) ||
                    trimmed.startsWith("Album:", ignoreCase = true) ||
                    trimmed.startsWith("Genre:", ignoreCase = true) ||
                    trimmed.startsWith("Series:", ignoreCase = true)
                )

                if (isNewKey) {
                    if (currentKey != null) {
                        val valStr = currentValue.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                        when {
                            currentKey == "title" || currentKey == "movie title" || currentKey == "show title" || currentKey == "show name" -> title = valStr
                            currentKey == "artist" || currentKey == "cast" || currentKey == "author" || currentKey == "host" -> artist = valStr
                            currentKey == "album title" || currentKey == "album" -> album = valStr
                            currentKey == "genre" || currentKey == "catagory" || currentKey == "category" -> genre = valStr
                            currentKey == "year" || currentKey == "release year" -> year = valStr.toIntOrNull()
                            currentKey == "parental rating" -> parentalRating = valStr
                            currentKey == "series" -> series = valStr
                            currentKey == "rating" || currentKey == "star rating" -> {
                                starRating = valStr.filter { it.isDigit() }.toIntOrNull()
                                    ?: if (valStr.contains("★")) valStr.count { it == '★' } else null
                            }
                            currentKey == "show description" || currentKey == "description" -> description = valStr
                        }
                    }
                    currentKey = trimmed.substring(0, colonIdx).trim().lowercase()
                    currentValue = StringBuilder(trimmed.substring(colonIdx + 1).trim())
                } else if (currentKey != null) {
                    currentValue.append("\n").append(line)
                }
            }
            if (currentKey != null) {
                val valStr = currentValue.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                when {
                    currentKey == "title" || currentKey == "movie title" || currentKey == "show title" || currentKey == "show name" -> title = valStr
                    currentKey == "artist" || currentKey == "cast" || currentKey == "author" || currentKey == "host" -> artist = valStr
                    currentKey == "album title" || currentKey == "album" -> album = valStr
                    currentKey == "genre" || currentKey == "catagory" || currentKey == "category" -> genre = valStr
                    currentKey == "year" || currentKey == "release year" -> year = valStr.toIntOrNull()
                    currentKey == "parental rating" -> parentalRating = valStr
                    currentKey == "series" -> series = valStr
                    currentKey == "rating" || currentKey == "star rating" -> {
                        starRating = valStr.filter { it.isDigit() }.toIntOrNull()
                            ?: if (valStr.contains("★")) valStr.count { it == '★' } else null
                    }
                    currentKey == "show description" || currentKey == "description" -> description = valStr
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ParsedMetadata(title, artist, album, genre, year, description, starRating, parentalRating, series)
}

data class ParsedAudiobookMetadata(
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val releaseYear: Int? = null,
    val bookSeries: String? = null,
    val description: String? = null,
    val chapters: List<AudiobookChapter> = emptyList()
)

fun parseTimeToMs(timeStr: String): Long {
    val parts = timeStr.trim().split(":")
    return when (parts.size) {
        3 -> {
            val h = parts[0].toLongOrNull() ?: 0L
            val m = parts[1].toLongOrNull() ?: 0L
            val s = parts[2].toLongOrNull() ?: 0L
            (h * 3600 + m * 60 + s) * 1000
        }
        2 -> {
            val m = parts[0].toLongOrNull() ?: 0L
            val s = parts[1].toLongOrNull() ?: 0L
            (m * 60 + s) * 1000
        }
        else -> 0L
    }
}

fun parseAudiobookMetadataTxt(txtFile: File): ParsedAudiobookMetadata {
    var title: String? = null
    var author: String? = null
    var genre: String? = null
    var releaseYear: Int? = null
    var bookSeries: String? = null
    var description: String? = null
    val chapters = mutableListOf<AudiobookChapter>()

    try {
        if (txtFile.exists()) {
            val lines = txtFile.readLines()
            var currentKey: String? = null
            var currentValue = StringBuilder()
            var inChaptersSection = false

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.equals("Chapters:", ignoreCase = true) || trimmed.equals("Chapters", ignoreCase = true)) {
                    if (currentKey != null) {
                        val valStr = currentValue.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                        when (currentKey) {
                            "title" -> title = valStr
                            "author", "artist" -> author = valStr
                            "genre", "catagory", "category" -> genre = valStr
                            "release year", "year" -> releaseYear = valStr.toIntOrNull()
                            "book series", "series" -> bookSeries = valStr
                            "description" -> description = valStr
                        }
                        currentKey = null
                    }
                    inChaptersSection = true
                    continue
                }

                if (inChaptersSection) {
                    if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                        val dashIdx = trimmed.indexOf(" - ")
                        if (dashIdx != -1) {
                            val timeStr = trimmed.substring(0, dashIdx).trim()
                            val rest = trimmed.substring(dashIdx + 3).trim()
                            val lastSpaceIdx = rest.lastIndexOf(" ")
                            val possibleNumStr = if (lastSpaceIdx != -1) rest.substring(lastSpaceIdx + 1).trim() else ""
                            val chNum = possibleNumStr.toIntOrNull()
                            val chTitleRaw = if (chNum != null && lastSpaceIdx != -1) {
                                rest.substring(0, lastSpaceIdx).trim()
                            } else {
                                rest
                            }
                            val chTitle = chTitleRaw.removeSurrounding("\"").removeSurrounding("'").trim()
                            val finalNum = chNum ?: (chapters.size + 1)
                            val timeMs = parseTimeToMs(timeStr)
                            chapters.add(AudiobookChapter(timeStr, timeMs, chTitle, finalNum))
                        }
                    }
                    continue
                }

                val colonIdx = trimmed.indexOf(":")
                val isNewKey = colonIdx != -1 && (
                    trimmed.startsWith("Title:", ignoreCase = true) ||
                    trimmed.startsWith("Author:", ignoreCase = true) ||
                    trimmed.startsWith("Artist:", ignoreCase = true) ||
                    trimmed.startsWith("Genre:", ignoreCase = true) ||
                    trimmed.startsWith("Release Year:", ignoreCase = true) ||
                    trimmed.startsWith("Year:", ignoreCase = true) ||
                    trimmed.startsWith("Book Series:", ignoreCase = true) ||
                    trimmed.startsWith("Series:", ignoreCase = true) ||
                    trimmed.startsWith("Description:", ignoreCase = true)
                )

                if (isNewKey) {
                    if (currentKey != null) {
                        val valStr = currentValue.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                        when (currentKey) {
                            "title" -> title = valStr
                            "author", "artist" -> author = valStr
                            "genre", "catagory", "category" -> genre = valStr
                            "release year", "year" -> releaseYear = valStr.toIntOrNull()
                            "book series", "series" -> bookSeries = valStr
                            "description" -> description = valStr
                        }
                    }
                    currentKey = trimmed.substring(0, colonIdx).trim().lowercase()
                    currentValue = StringBuilder(trimmed.substring(colonIdx + 1).trim())
                } else if (currentKey != null) {
                    currentValue.append("\n").append(line)
                }
            }
            if (currentKey != null && !inChaptersSection) {
                val valStr = currentValue.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                when (currentKey) {
                    "title" -> title = valStr
                    "author", "artist" -> author = valStr
                    "genre", "catagory", "category" -> genre = valStr
                    "release year", "year" -> releaseYear = valStr.toIntOrNull()
                    "book series", "series" -> bookSeries = valStr
                    "description" -> description = valStr
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ParsedAudiobookMetadata(title, author, genre, releaseYear, bookSeries, description, chapters)
}

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

    fun extractRandomVideoThumbnail(context: Context, uri: Uri): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val randomTimeUs = if (durationMs > 2000L) {
                (Math.random() * (durationMs - 1000L) + 1000L).toLong() * 1000L
            } else {
                1000000L
            }
            val bitmap = retriever.getFrameAtTime(randomTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap != null) {
                val imageFile = File(context.cacheDir, "video_thumb_${System.currentTimeMillis()}.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                return Uri.fromFile(imageFile).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) { }
        }
        return ""
    }

    fun extractFirstVideoThumbnail(context: Context, uri: Uri): String {
        val uriStr = uri.toString().lowercase()
        if (uriStr.endsWith(".jpg") || uriStr.endsWith(".jpeg") || uriStr.endsWith(".png") || uriStr.endsWith(".webp")) {
            return uri.toString()
        }
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap != null) {
                val imageFile = File(context.cacheDir, "short_first_thumb_${System.currentTimeMillis()}.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                return Uri.fromFile(imageFile).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) { }
        }
        return ""
    }

    fun extractTvEpisodeThumbnail(context: Context, uri: Uri): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val targetTimeMs = if (durationMs >= 90000L) {
                90000L
            } else if (durationMs > 2000L) {
                durationMs / 2
            } else {
                1000L
            }
            val targetTimeUs = targetTimeMs * 1000L

            val bitmap = retriever.getFrameAtTime(targetTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap != null) {
                val thumbDir = File(context.filesDir, "tv_thumbnails")
                thumbDir.mkdirs()
                val imageFile = File(thumbDir, "tv_ep_${System.currentTimeMillis()}_${(Math.random()*1000).toInt()}.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                return Uri.fromFile(imageFile).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) { }
        }
        return ""
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

            // Extract embedded picture artwork directly from ID3/MP4/FLAC file metadata
            val picture = retriever.embeddedPicture
            if (picture != null && picture.isNotEmpty()) {
                val coverDir = File(context.filesDir, "album_covers")
                coverDir.mkdirs()
                val fileHash = kotlin.math.abs(uri.toString().hashCode())
                val imageFile = File(coverDir, "cover_${fileHash}.jpg")
                if (!imageFile.exists() || imageFile.length() == 0L) {
                    FileOutputStream(imageFile).use { out ->
                        out.write(picture)
                    }
                }
                artworkUri = Uri.fromFile(imageFile).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

        if (artworkUri.isBlank() && isVideo) {
            artworkUri = extractRandomVideoThumbnail(context, uri)
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

    suspend fun cleanupMissingFiles(context: Context, repository: MediaRepository) {
        try {
            val allSongs = repository.getAllSongs().first()
            for (song in allSongs) {
                var exists = false
                val uri = Uri.parse(song.uri)

                if (uri.scheme == "file") {
                    val filePath = uri.path ?: song.filePath
                    val file = File(filePath)
                    exists = file.exists()
                } else if (uri.scheme == "content") {
                    try {
                        context.contentResolver.openInputStream(uri)?.use {
                            exists = true
                        }
                    } catch (e: Exception) {
                        exists = false
                    }
                } else {
                    val file = File(song.filePath)
                    exists = file.exists()
                }

                if (!exists) {
                    repository.deleteSong(song)

                    if (song.albumTitle.isNotBlank()) {
                        val remainingSongs = repository.getSongsByAlbum(song.albumTitle).first()
                        if (remainingSongs.isEmpty()) {
                            val album = repository.getAlbumByTitle(song.albumTitle)
                            if (album != null) {
                                repository.deleteAlbum(album)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun cleanupNonMusicSongs(repository: MediaRepository) {
        try {
            val allSongs = repository.getAllSongs().first()
            for (song in allSongs) {
                val path = song.filePath.lowercase()
                if (path.contains("/tv-shows/") || path.contains("/movies/") || path.contains("/audiobooks/") || path.contains("/podcasts/")) {
                    repository.deleteSong(song)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateTvShowsAndEpisodes(repository: MediaRepository) {
        try {
            val allShows = repository.getAllTvShows().first()
            val showGrouped = allShows.groupBy { it.title.trim().lowercase() }
            for ((_, showList) in showGrouped) {
                if (showList.size > 1) {
                    val duplicates = showList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteTvShow(duplicate)
                    }
                }
            }

            val allEpisodes = repository.getAllTvEpisodes().first()
            val epGrouped = allEpisodes.groupBy {
                Triple(it.showTitle.trim().lowercase(), it.seasonNumber, it.episodeNumber)
            }
            for ((_, episodeList) in epGrouped) {
                if (episodeList.size > 1) {
                    val duplicates = episodeList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteTvEpisode(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateSongsAndMusicVideos(repository: MediaRepository) {
        try {
            val allSongs = repository.getAllSongs().first()

            val audioSongs = allSongs.filter { !it.isMusicVideo }
            val audioGrouped = audioSongs.groupBy {
                Triple(it.title.trim().lowercase(), it.artist.trim().lowercase(), it.albumTitle.trim().lowercase())
            }
            for ((_, songList) in audioGrouped) {
                if (songList.size > 1) {
                    val duplicates = songList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteSong(duplicate)
                    }
                }
            }

            val musicVideos = allSongs.filter { it.isMusicVideo }
            val mvGrouped = musicVideos.groupBy {
                Pair(it.title.trim().lowercase(), it.artist.trim().lowercase())
            }
            for ((_, mvList) in mvGrouped) {
                if (mvList.size > 1) {
                    val duplicates = mvList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteSong(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateAlbums(repository: MediaRepository) {
        try {
            val allAlbums = repository.getAllAlbums().first()
            val grouped = allAlbums.groupBy { it.title.trim().lowercase() }
            for ((_, albumList) in grouped) {
                if (albumList.size > 1) {
                    val duplicates = albumList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteAlbum(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateMovies(repository: MediaRepository) {
        try {
            val allMovies = repository.getAllMovies().first()

            val titleGrouped = allMovies.groupBy { it.title.trim().lowercase() }
            for ((_, movieList) in titleGrouped) {
                if (movieList.size > 1) {
                    val duplicates = movieList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteMovie(duplicate)
                    }
                }
            }

            val remainingMovies = repository.getAllMovies().first()
            val pathGrouped = remainingMovies.groupBy { (it.videoPath.ifBlank { it.uri }).trim().lowercase() }
            for ((key, movieList) in pathGrouped) {
                if (key.isNotBlank() && movieList.size > 1) {
                    val duplicates = movieList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteMovie(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicatePodcastsAndEpisodes(repository: MediaRepository) {
        try {
            val allShows = repository.getAllPodcastShows().first()
            val showGrouped = allShows.groupBy { it.title.trim().lowercase() }
            for ((_, showList) in showGrouped) {
                if (showList.size > 1) {
                    val duplicates = showList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deletePodcastShow(duplicate)
                    }
                }
            }

            val allEpisodes = repository.getAllPodcastEpisodes().first()
            val epGrouped = allEpisodes.groupBy {
                Triple(it.podcastName.trim().lowercase(), it.seasonNumber, it.episodeNumber)
            }
            for ((_, episodeList) in epGrouped) {
                if (episodeList.size > 1) {
                    val duplicates = episodeList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deletePodcastEpisode(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateAudiobooks(repository: MediaRepository) {
        try {
            val allAudiobooks = repository.getAllAudiobooks().first()
            val grouped = allAudiobooks.groupBy { it.title.trim().lowercase() }
            for ((_, bookList) in grouped) {
                if (bookList.size > 1) {
                    val duplicates = bookList.drop(1)
                    for (duplicate in duplicates) {
                        repository.deleteAudiobook(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deduplicateShorts(repository: MediaRepository) {
        try {
            val allShorts = repository.getAllShorts().first()
            val grouped = allShorts.groupBy { it.title.trim().lowercase() }
            for ((_, shortList) in grouped) {
                if (shortList.size > 1) {
                    val best = shortList.firstOrNull { it.isSaved } ?: shortList.first()
                    val duplicates = shortList.filter { it.id != best.id }
                    for (duplicate in duplicates) {
                        repository.deleteShort(duplicate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun runAllDeduplications(repository: MediaRepository) {
        deduplicateSongsAndMusicVideos(repository)
        deduplicateAlbums(repository)
        deduplicateTvShowsAndEpisodes(repository)
        deduplicateMovies(repository)
        deduplicatePodcastsAndEpisodes(repository)
        deduplicateAudiobooks(repository)
        deduplicateShorts(repository)
    }

    suspend fun populateMissingArtwork(context: Context, repository: MediaRepository) {
        try {
            val allSongs = repository.getAllSongs().first()
            for (song in allSongs) {
                if (song.artworkUri.isBlank() && !song.isMusicVideo) {
                    var foundArt = ""
                    val uri = Uri.parse(song.uri)
                    val extracted = extractMetadata(context, uri)
                    if (extracted.artworkUri.isNotBlank()) {
                        foundArt = extracted.artworkUri
                    } else {
                        val filePath = if (uri.scheme == "file") uri.path ?: song.filePath else song.filePath
                        val file = File(filePath)
                        if (file.exists()) {
                            val parentFolder = file.parentFile
                            val folderImages = parentFolder?.listFiles { f ->
                                f.isFile && setOf("jpg", "jpeg", "png", "webp").contains(f.extension.lowercase())
                            } ?: emptyArray()
                            foundArt = folderImages.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""
                        }
                    }

                    if (foundArt.isNotBlank()) {
                        val updatedSong = song.copy(artworkUri = foundArt)
                        repository.updateSong(updatedSong)
                    }
                }
            }

            // Populate missing Album covers
            val allAlbums = repository.getAllAlbums().first()
            val updatedSongs = repository.getAllSongs().first()
            for (album in allAlbums) {
                if (album.albumCoverUri.isBlank()) {
                    val albumSongs = updatedSongs.filter { it.albumTitle.equals(album.title, ignoreCase = true) }
                    val coverArt = albumSongs.firstOrNull { it.artworkUri.isNotBlank() }?.artworkUri ?: ""
                    if (coverArt.isNotBlank()) {
                        repository.updateAlbum(album.copy(albumCoverUri = coverArt))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun scanAndImportATunesDirectory(context: Context, repository: MediaRepository) {
        cleanupMissingFiles(context, repository)
        cleanupNonMusicSongs(repository)
        runAllDeduplications(repository)

        val libDirs = ATunesLibrarySyncEngine.getLibraryDirectories(context)

        val audioExtensions = setOf("mp3", "m4a", "flac", "wav", "aac", "ogg", "m4b", "opus")
        val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "m4v")

        for (libDir in libDirs) {
            if (!libDir.exists() || !libDir.isDirectory) continue

            // 1. Dedicated TV-Shows Directory & Season Scanner
            try {
                val tvShowsDir = File(libDir, "TV-Shows")
                if (tvShowsDir.exists() && tvShowsDir.isDirectory) {
                    val showFolders = tvShowsDir.listFiles { file -> file.isDirectory } ?: emptyArray()
                    for (showFolder in showFolders) {
                        val showTitle = showFolder.name
                        if (showTitle.isBlank() || showTitle == "Unspecified") continue

                        val showImageFiles = showFolder.listFiles { file ->
                            file.isFile && setOf("jpg", "jpeg", "png", "webp").contains(file.extension.lowercase())
                        }
                        val posterUri = showImageFiles?.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""

                        val showMetaFile = File(showFolder, "Metadata.txt")
                        val showParsed = if (showMetaFile.exists()) parseMetadataTxt(showMetaFile) else null

                        val finalShowTitle = showParsed?.title?.takeIf { it.isNotBlank() } ?: showTitle
                        val finalRating = showParsed?.parentalRating ?: ""
                        val finalStars = showParsed?.starRating ?: 0
                        val finalDesc = showParsed?.description ?: ""

                        var tvShow = repository.getTvShowByTitle(finalShowTitle) ?: repository.getTvShowByTitle(showTitle)
                        if (tvShow == null) {
                            val newShow = TvShowEntity(
                                title = finalShowTitle,
                                releaseYear = 2024,
                                parentalRating = finalRating.ifBlank { "TV-14" },
                                logoThumbnailUri = posterUri,
                                description = finalDesc,
                                starRating = finalStars
                            )
                            val id = repository.insertTvShow(newShow)
                            tvShow = newShow.copy(id = id)
                        } else {
                            val updated = tvShow.copy(
                                title = finalShowTitle,
                                parentalRating = if (finalRating.isNotBlank()) finalRating else tvShow.parentalRating,
                                starRating = if (finalStars > 0) finalStars else tvShow.starRating,
                                description = if (finalDesc.isNotBlank()) finalDesc else tvShow.description,
                                logoThumbnailUri = if (posterUri.isNotBlank()) posterUri else tvShow.logoThumbnailUri
                            )
                            repository.updateTvShow(updated)
                        }

                        val subFolders = showFolder.listFiles { file -> file.isDirectory } ?: emptyArray()
                        for (seasonFolder in subFolders) {
                            val folderName = seasonFolder.name
                            val fallbackSeasonNum = folderName.lowercase().filter { it.isDigit() }.toIntOrNull() ?: 1

                            val seasonMetaFile = File(seasonFolder, "Metadata.txt")
                            val (parsedSeasonNum, parsedEpList) = if (seasonMetaFile.exists()) parseSeasonMetadataTxt(seasonMetaFile) else Pair(null, emptyList())
                            val finalSeasonNum = parsedSeasonNum ?: fallbackSeasonNum

                            val epFiles = seasonFolder.listFiles { file ->
                                file.isFile && videoExtensions.contains(file.extension.lowercase())
                            } ?: emptyArray()

                            for (file in epFiles) {
                                val epUriStr = Uri.fromFile(file).toString()
                                val existingEp = repository.getAllTvEpisodes().first().firstOrNull { it.uri == epUriStr || it.videoPath == epUriStr }

                                val epNameClean = cleanTitleFromFilename(file.name)
                                val fileEpNum = epNameClean.lowercase().filter { char -> char.isDigit() }.toIntOrNull()
                                val matchedParsedEp = parsedEpList.firstOrNull {
                                    it.fileName.equals(file.name, ignoreCase = true) ||
                                    it.fileName.equals(file.nameWithoutExtension, ignoreCase = true) ||
                                    (fileEpNum != null && it.episodeNumber == fileEpNum)
                                }

                                val finalEpTitle = matchedParsedEp?.title?.takeIf { it.isNotBlank() } ?: epNameClean
                                val finalEpNum = matchedParsedEp?.episodeNumber ?: (fileEpNum ?: 1)
                                val epThumb = extractTvEpisodeThumbnail(context, Uri.fromFile(file))

                                if (existingEp == null) {
                                    val newEpisode = TvEpisodeEntity(
                                        title = finalEpTitle,
                                        showTitle = finalShowTitle,
                                        seasonNumber = finalSeasonNum,
                                        episodeNumber = finalEpNum,
                                        videoPath = epUriStr,
                                        uri = epUriStr,
                                        thumbnailUri = epThumb
                                    )
                                    repository.insertTvEpisode(newEpisode)
                                } else {
                                    val updatedEp = existingEp.copy(
                                        showTitle = finalShowTitle,
                                        seasonNumber = finalSeasonNum,
                                        episodeNumber = finalEpNum,
                                        title = finalEpTitle,
                                        thumbnailUri = if (existingEp.thumbnailUri.isBlank()) epThumb else existingEp.thumbnailUri
                                    )
                                    repository.updateTvEpisode(updatedEp)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Scan Movies folders
            try {
                val moviesDir = File(libDir, "Movies")
                if (moviesDir.exists() && moviesDir.isDirectory) {
                    val movieFolders = moviesDir.listFiles { file -> file.isDirectory } ?: emptyArray()
                    for (movieFolder in movieFolders) {
                        val folderName = movieFolder.name
                        if (folderName.isBlank() || folderName == "Unspecified") continue

                        val videoFiles = movieFolder.listFiles { file ->
                            file.isFile && videoExtensions.contains(file.extension.lowercase())
                        } ?: emptyArray()

                        val imageFiles = movieFolder.listFiles { file ->
                            file.isFile && setOf("jpg", "jpeg", "png", "webp").contains(file.extension.lowercase())
                        } ?: emptyArray()

                        val metaFile = File(movieFolder, "Metadata.txt")
                        val parsed = if (metaFile.exists()) parseMetadataTxt(metaFile) else null

                        val posterUri = imageFiles.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""

                        val finalMovieTitle = parsed?.title?.takeIf { it.isNotBlank() } ?: folderName
                        val existingByTitle = repository.getAllMovies().first().firstOrNull { it.title.equals(finalMovieTitle, ignoreCase = true) || it.title.equals(folderName, ignoreCase = true) }
                        if (existingByTitle != null && posterUri.isNotBlank()) {
                            repository.updateMovie(existingByTitle.copy(thumbnailUri = posterUri))
                        }

                        for (videoFile in videoFiles) {
                            val videoUriStr = Uri.fromFile(videoFile).toString()
                            val videoCleanTitle = cleanTitleFromFilename(videoFile.name)

                            val finalTitle = parsed?.title?.takeIf { it.isNotBlank() } ?: videoCleanTitle
                            val finalRating = parsed?.parentalRating ?: "PG"
                            val finalYear = parsed?.year ?: 2024
                            val finalStars = parsed?.starRating ?: 0
                            val finalDesc = parsed?.description ?: ""
                            val finalPoster = if (posterUri.isNotBlank()) posterUri else extractRandomVideoThumbnail(context, Uri.fromFile(videoFile))

                            val existingMovie = repository.getAllMovies().first().firstOrNull {
                                it.uri == videoUriStr || it.videoPath == videoUriStr || it.title.equals(finalTitle, ignoreCase = true)
                            }

                            if (existingMovie == null) {
                                val newMovie = MovieEntity(
                                    title = finalTitle,
                                    artists = "",
                                    releaseYear = if (finalYear > 0) finalYear else 2024,
                                    parentalRating = finalRating.ifBlank { "PG" },
                                    thumbnailUri = finalPoster,
                                    description = finalDesc,
                                    starRating = finalStars,
                                    videoPath = videoUriStr,
                                    uri = videoUriStr
                                )
                                repository.insertMovie(newMovie)
                            } else {
                                val updatedMovie = existingMovie.copy(
                                    title = finalTitle,
                                    releaseYear = if (finalYear > 0) finalYear else existingMovie.releaseYear,
                                    parentalRating = if (finalRating.isNotBlank()) finalRating else existingMovie.parentalRating,
                                    starRating = if (finalStars > 0) finalStars else existingMovie.starRating,
                                    description = if (finalDesc.isNotBlank()) finalDesc else existingMovie.description,
                                    thumbnailUri = if (posterUri.isNotBlank()) posterUri else existingMovie.thumbnailUri,
                                    videoPath = videoUriStr,
                                    uri = videoUriStr
                                )
                                repository.updateMovie(updatedMovie)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Dedicated Podcasts Directory & Season Scanner
            try {
                val podcastsDir = File(libDir, "Podcasts")
                if (podcastsDir.exists() && podcastsDir.isDirectory) {
                    val podcastFolders = podcastsDir.listFiles { file -> file.isDirectory } ?: emptyArray()
                    for (podFolder in podcastFolders) {
                        val podTitle = podFolder.name
                        if (podTitle.isBlank() || podTitle == "Unspecified") continue

                        val imageFiles = podFolder.listFiles { file ->
                            file.isFile && setOf("jpg", "jpeg", "png", "webp").contains(file.extension.lowercase())
                        } ?: emptyArray()
                        val artUri = imageFiles.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""

                        val metaFile = File(podFolder, "Metadata.txt")
                        val parsed = if (metaFile.exists()) parseMetadataTxt(metaFile) else null

                        val finalTitle = parsed?.title?.takeIf { it.isNotBlank() } ?: podTitle
                        val finalHost = parsed?.artist?.takeIf { it.isNotBlank() } ?: "Unknown Creator"
                        val finalCategory = parsed?.genre?.takeIf { it.isNotBlank() } ?: "Podcast"
                        val finalStars = parsed?.starRating ?: 0
                        val finalDesc = parsed?.description ?: ""

                        var existingPod = repository.getPodcastShowByTitle(finalTitle) ?: repository.getPodcastShowByTitle(podTitle)
                        if (existingPod == null) {
                            val newPod = PodcastShowEntity(
                                title = finalTitle,
                                creatorHost = finalHost,
                                category = finalCategory,
                                logoThumbnailUri = artUri,
                                description = finalDesc,
                                starRating = finalStars
                            )
                            val id = repository.insertPodcastShow(newPod)
                            existingPod = newPod.copy(id = id)
                        } else {
                            val updated = existingPod.copy(
                                title = finalTitle,
                                creatorHost = if (finalHost.isNotBlank() && finalHost != "Unknown Creator") finalHost else existingPod.creatorHost,
                                category = if (finalCategory.isNotBlank() && finalCategory != "Podcast") finalCategory else existingPod.category,
                                starRating = if (finalStars > 0) finalStars else existingPod.starRating,
                                description = if (finalDesc.isNotBlank()) finalDesc else existingPod.description,
                                logoThumbnailUri = if (artUri.isNotBlank()) artUri else existingPod.logoThumbnailUri
                            )
                            repository.updatePodcastShow(updated)
                        }

                        val directAudioFiles = podFolder.listFiles { file ->
                            file.isFile && audioExtensions.contains(file.extension.lowercase())
                        } ?: emptyArray()

                        for (file in directAudioFiles) {
                            val epUriStr = Uri.fromFile(file).toString()
                            val existingEp = repository.getAllPodcastEpisodes().first().firstOrNull { it.uri == epUriStr || it.audioPath == epUriStr }
                            val epNameClean = cleanTitleFromFilename(file.name)
                            val epNum = epNameClean.lowercase().filter { char -> char.isDigit() }.toIntOrNull() ?: 1

                            if (existingEp == null) {
                                val newEpisode = PodcastEpisodeEntity(
                                    title = epNameClean,
                                    podcastName = finalTitle,
                                    seasonNumber = 1,
                                    episodeNumber = epNum,
                                    audioPath = epUriStr,
                                    uri = epUriStr
                                )
                                repository.insertPodcastEpisode(newEpisode)
                            } else {
                                val updatedEp = existingEp.copy(
                                    podcastName = finalTitle,
                                    title = if (existingEp.title.isNotBlank() && existingEp.title != "Untitled") existingEp.title else epNameClean
                                )
                                repository.updatePodcastEpisode(updatedEp)
                            }
                        }

                        val seasonSubFolders = podFolder.listFiles { file -> file.isDirectory } ?: emptyArray()
                        for (seasonFolder in seasonSubFolders) {
                            val folderName = seasonFolder.name
                            val fallbackSeasonNum = folderName.lowercase().filter { it.isDigit() }.toIntOrNull() ?: 1

                            val seasonMetaFile = File(seasonFolder, "Metadata.txt")
                            val (parsedSeasonNum, parsedEpList) = if (seasonMetaFile.exists()) parseSeasonMetadataTxt(seasonMetaFile) else Pair(null, emptyList())
                            val finalSeasonNum = parsedSeasonNum ?: fallbackSeasonNum

                            val epFiles = seasonFolder.listFiles { file ->
                                file.isFile && audioExtensions.contains(file.extension.lowercase())
                            } ?: emptyArray()

                            for (file in epFiles) {
                                val epUriStr = Uri.fromFile(file).toString()
                                val existingEp = repository.getAllPodcastEpisodes().first().firstOrNull { it.uri == epUriStr || it.audioPath == epUriStr }

                                val epNameClean = cleanTitleFromFilename(file.name)
                                val fileEpNum = epNameClean.lowercase().filter { char -> char.isDigit() }.toIntOrNull()
                                val matchedParsedEp = parsedEpList.firstOrNull {
                                    it.fileName.equals(file.name, ignoreCase = true) ||
                                    it.fileName.equals(file.nameWithoutExtension, ignoreCase = true) ||
                                    (fileEpNum != null && it.episodeNumber == fileEpNum)
                                }

                                val finalEpTitle = matchedParsedEp?.title?.takeIf { it.isNotBlank() } ?: epNameClean
                                val finalEpNum = matchedParsedEp?.episodeNumber ?: (fileEpNum ?: 1)

                                if (existingEp == null) {
                                    val newEpisode = PodcastEpisodeEntity(
                                        title = finalEpTitle,
                                        podcastName = finalTitle,
                                        seasonNumber = finalSeasonNum,
                                        episodeNumber = finalEpNum,
                                        audioPath = epUriStr,
                                        uri = epUriStr
                                    )
                                    repository.insertPodcastEpisode(newEpisode)
                                } else {
                                    val updatedEp = existingEp.copy(
                                        podcastName = finalTitle,
                                        seasonNumber = finalSeasonNum,
                                        episodeNumber = finalEpNum,
                                        title = finalEpTitle
                                    )
                                    repository.updatePodcastEpisode(updatedEp)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Scan Audiobooks folders
            try {
                val audiobookDir = File(libDir, "Audiobooks")
                if (audiobookDir.exists() && audiobookDir.isDirectory) {
                    val existingBooks = repository.getAllAudiobooks().first()

                    suspend fun scanAudiobooksRecursive(dir: File) {
                        val files = dir.listFiles() ?: return
                        for (file in files) {
                            if (file.isDirectory) {
                                scanAudiobooksRecursive(file)
                            } else if (file.isFile && !file.name.endsWith(".txt", ignoreCase = true)) {
                                val ext = file.extension.lowercase()
                                if (audioExtensions.contains(ext)) {
                                    val uriStr = Uri.fromFile(file).toString()
                                    val existingBook = existingBooks.firstOrNull { it.uri == uriStr || it.audioPath == uriStr }

                                    val cleanTitle = cleanTitleFromFilename(file.name)
                                    val parentFolder = file.parentFile
                                    val imageFiles = parentFolder?.listFiles { f ->
                                        f.isFile && setOf("jpg", "jpeg", "png", "webp").contains(f.extension.lowercase())
                                    } ?: emptyArray()
                                    val coverUri = imageFiles.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""

                                    val metaFile = File(parentFolder ?: dir, "Metadata.txt")
                                    val parsed = if (metaFile.exists()) parseAudiobookMetadataTxt(metaFile) else null

                                    val finalTitle = parsed?.title?.takeIf { it.isNotBlank() } ?: (existingBook?.title?.takeIf { it.isNotBlank() } ?: cleanTitle)
                                    val finalAuthor = parsed?.author?.takeIf { it.isNotBlank() } ?: (existingBook?.author?.takeIf { it.isNotBlank() } ?: (parentFolder?.name?.takeIf { it != "Audiobooks" && it != "Unspecified" } ?: "Unknown Author"))
                                    val finalGenre = parsed?.genre?.takeIf { it.isNotBlank() } ?: (existingBook?.genre?.takeIf { it.isNotBlank() } ?: "Audiobook")
                                    val finalYear = parsed?.releaseYear ?: (existingBook?.publicationYear?.takeIf { it > 0 } ?: 2024)
                                    val finalSeries = parsed?.bookSeries?.takeIf { it.isNotBlank() } ?: (existingBook?.bookSeries?.takeIf { it.isNotBlank() } ?: "Unspecified")
                                    val finalDescription = parsed?.description?.takeIf { it.isNotBlank() } ?: (existingBook?.description ?: "")
                                    val finalChaptersJson = if (parsed?.chapters?.isNotEmpty() == true) {
                                        try { Json.encodeToString(parsed.chapters) } catch (e: Exception) { "" }
                                    } else (existingBook?.chaptersJson ?: "")

                                    if (existingBook == null) {
                                        val newBook = AudiobookEntity(
                                            title = finalTitle,
                                            author = finalAuthor,
                                            bookSeries = finalSeries,
                                            bookNumberInSeries = 0,
                                            genre = finalGenre,
                                            publicationYear = finalYear,
                                            portraitCoverUri = coverUri,
                                            audioPath = uriStr,
                                            uri = uriStr,
                                            description = finalDescription,
                                            chaptersJson = finalChaptersJson
                                        )
                                        repository.insertAudiobook(newBook)
                                    } else {
                                        val updatedBook = existingBook.copy(
                                            title = finalTitle,
                                            author = finalAuthor,
                                            bookSeries = finalSeries,
                                            genre = finalGenre,
                                            publicationYear = finalYear,
                                            portraitCoverUri = if (coverUri.isNotBlank()) coverUri else existingBook.portraitCoverUri,
                                            description = if (finalDescription.isNotBlank()) finalDescription else existingBook.description,
                                            chaptersJson = if (finalChaptersJson.isNotBlank()) finalChaptersJson else existingBook.chaptersJson
                                        )
                                        repository.updateAudiobook(updatedBook)
                                    }
                                }
                            }
                        }
                    }
                    scanAudiobooksRecursive(audiobookDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 5. Scan Music Videos folders
            try {
                val mvDir = File(libDir, "Music/Music Videos")
                if (mvDir.exists() && mvDir.isDirectory) {
                    val mvFolders = mvDir.listFiles { file -> file.isDirectory } ?: emptyArray()
                    for (mvFolder in mvFolders) {
                        val mvTitle = mvFolder.name
                        val imageFiles = mvFolder.listFiles { file ->
                            file.isFile && setOf("jpg", "jpeg", "png", "webp").contains(file.extension.lowercase())
                        } ?: emptyArray()
                        val mvArtUri = imageFiles.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""

                        val metaFile = File(mvFolder, "Metadata.txt")
                        val parsed = if (metaFile.exists()) parseMetadataTxt(metaFile) else null

                        val finalTitle = parsed?.title?.takeIf { it.isNotBlank() } ?: mvTitle
                        val finalArtist = parsed?.artist ?: ""
                        val finalAlbum = parsed?.album ?: ""
                        val finalGenre = parsed?.genre ?: ""
                        val finalYear = parsed?.year ?: 0

                        val allMusicVideos = repository.getMusicVideos().first()
                        val existingMv = allMusicVideos.firstOrNull {
                            it.title.equals(finalTitle, ignoreCase = true) ||
                            it.title.equals(mvTitle, ignoreCase = true) ||
                            File(Uri.parse(it.uri).path ?: "").parentFile?.name.equals(mvTitle, ignoreCase = true)
                        }

                        if (existingMv != null) {
                            val updated = existingMv.copy(
                                title = finalTitle,
                                artist = if (finalArtist.isNotBlank()) finalArtist else existingMv.artist,
                                albumTitle = if (finalAlbum.isNotBlank()) finalAlbum else existingMv.albumTitle,
                                genre = if (finalGenre.isNotBlank()) finalGenre else existingMv.genre,
                                year = if (finalYear > 0) finalYear else existingMv.year,
                                artworkUri = if (mvArtUri.isNotBlank()) mvArtUri else existingMv.artworkUri,
                                isMusicVideo = true
                            )
                            repository.updateSong(updated)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 6. Scan Shorts folder (videos AND images)
            try {
                val shortsDir = File(libDir, "Shorts")
                if (shortsDir.exists() && shortsDir.isDirectory) {
                    val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif")
                    val shortFiles = shortsDir.walkTopDown().filter { file ->
                        file.isFile && (videoExtensions.contains(file.extension.lowercase()) || imageExtensions.contains(file.extension.lowercase()))
                    }.toList()

                    for (file in shortFiles) {
                        val uriStr = Uri.fromFile(file).toString()
                        val existing = repository.getShortByUri(uriStr)
                        val cleanTitle = cleanTitleFromFilename(file.name)
                        if (existing == null) {
                            repository.insertShort(
                                ShortVideoEntity(
                                    title = cleanTitle,
                                    filePath = uriStr,
                                    uri = uriStr
                                )
                            )
                        } else {
                            val updated = existing.copy(
                                title = cleanTitle,
                                filePath = uriStr,
                                uri = uriStr
                            )
                            repository.updateShort(updated)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            suspend fun scanRecursive(dir: File) {
                val files = dir.listFiles() ?: return
                val hasMetaTxt = files.any { it.name.equals("Metadata.txt", ignoreCase = true) || it.name.endsWith(".txt", ignoreCase = true) }
                val mediaFiles = files.filter { it.isFile && !it.name.endsWith(".txt") && (audioExtensions.contains(it.extension.lowercase()) || videoExtensions.contains(it.extension.lowercase())) }

                if (mediaFiles.isNotEmpty() && !hasMetaTxt) {
                    try {
                        val metaFile = File(dir, "Metadata.txt")
                        val content = buildString {
                            appendLine("Folder: ${dir.name}")
                            appendLine("Media Files:")
                            mediaFiles.forEach { mf ->
                                appendLine(" - ${mf.name}")
                            }
                        }
                        metaFile.writeText(content)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                for (file in files) {
                    if (file.isDirectory) {
                        scanRecursive(file)
                    } else if (file.isFile && file.name != "Metadata.txt" && !file.name.endsWith(".txt")) {
                        val ext = file.extension.lowercase()
                        if (audioExtensions.contains(ext) || videoExtensions.contains(ext)) {
                            val fileUriStr = Uri.fromFile(file).toString()
                            val existingSong = repository.getSongByFilePath(fileUriStr)
                            if (existingSong == null) {
                                val isMv = file.path.contains("/Music Videos/", ignoreCase = true) || file.parentFile?.name?.equals("Music Videos", ignoreCase = true) == true
                                var songEntity = createSongEntity(context, Uri.fromFile(file)).copy(isMusicVideo = isMv)

                                if (songEntity.artworkUri.isBlank()) {
                                    val parentFolder = file.parentFile
                                    val folderImages = parentFolder?.listFiles { f ->
                                        f.isFile && setOf("jpg", "jpeg", "png", "webp").contains(f.extension.lowercase())
                                    } ?: emptyArray()
                                    val folderArt = folderImages.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""
                                    if (folderArt.isNotBlank()) {
                                        songEntity = songEntity.copy(artworkUri = folderArt)
                                    }
                                }

                                repository.insertSong(songEntity)

                                if (songEntity.albumTitle.isNotBlank() && songEntity.albumTitle != "Unspecified") {
                                    val songsInAlbum = repository.getSongsByAlbum(songEntity.albumTitle).first() + songEntity
                                    val firstSongArt = songsInAlbum.firstOrNull { it.artworkUri.isNotBlank() }?.artworkUri ?: songEntity.artworkUri

                                    val existingAlbum = repository.getAlbumByTitle(songEntity.albumTitle)
                                    if (existingAlbum == null) {
                                        val newAlbum = AlbumEntity(
                                            title = songEntity.albumTitle,
                                            albumArtist = songEntity.albumArtist,
                                            genre = songEntity.genre,
                                            releaseYear = songEntity.year,
                                            albumCoverUri = firstSongArt
                                        )
                                        repository.insertAlbum(newAlbum)
                                    } else if (existingAlbum.albumCoverUri.isBlank() && firstSongArt.isNotBlank()) {
                                        val updated = existingAlbum.copy(
                                            albumCoverUri = firstSongArt
                                        )
                                        repository.updateAlbum(updated)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Only scan Music directory for Songs and Music Videos!
            val musicDir = File(libDir, "Music")
            if (musicDir.exists() && musicDir.isDirectory) {
                scanRecursive(musicDir)
            }
        }

        // Run Deduplication and populate missing artwork after scanning
        runAllDeduplications(repository)
        populateMissingArtwork(context, repository)
    }

    fun createSongEntity(context: Context, uri: Uri): SongEntity {
        takePersistablePermission(context, uri)
        val extracted = extractMetadata(context, uri)
        val fileName = getFileName(context, uri)
        val title = extracted.title.ifBlank { cleanTitleFromFilename(fileName) }

        var artwork = extracted.artworkUri
        if (artwork.isBlank()) {
            val file = if (uri.scheme == "file") File(uri.path ?: "") else null
            if (file != null && file.exists()) {
                val parentFolder = file.parentFile
                val folderImages = parentFolder?.listFiles { f ->
                    f.isFile && setOf("jpg", "jpeg", "png", "webp").contains(f.extension.lowercase())
                } ?: emptyArray()
                artwork = folderImages.firstOrNull()?.let { Uri.fromFile(it).toString() } ?: ""
            }
        }

        if (extracted.isVideo && artwork.isBlank()) {
            artwork = extractRandomVideoThumbnail(context, uri)
        }

        val artistName = extracted.artistOrCreator.ifBlank { "Unknown Artist" }
        val albumTitle = extracted.albumOrShowOrSeries.ifBlank { "Unspecified" }
        val genreName = extracted.genre.ifBlank { "Unspecified" }

        return SongEntity(
            title = title,
            artist = artistName,
            albumArtist = artistName,
            albumTitle = albumTitle,
            composer = "",
            genre = genreName,
            year = if (extracted.releaseYear > 0) extracted.releaseYear else 2024,
            trackNumber = extracted.trackNumber,
            filePath = uri.toString(),
            uri = uri.toString(),
            duration = extracted.durationMs,
            artworkUri = artwork,
            isMusicVideo = extracted.isVideo
        )
    }

    fun createTvEpisodeEntity(context: Context, uri: Uri): TvEpisodeEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)
        val thumb = extractTvEpisodeThumbnail(context, uri)

        return TvEpisodeEntity(
            title = title,
            showTitle = "Unspecified",
            seasonNumber = 0,
            episodeNumber = 0,
            videoPath = uri.toString(),
            uri = uri.toString(),
            thumbnailUri = thumb
        )
    }

    fun createMovieEntity(context: Context, uri: Uri): MovieEntity {
        takePersistablePermission(context, uri)
        val fileName = getFileName(context, uri)
        val title = cleanTitleFromFilename(fileName)
        val poster = extractRandomVideoThumbnail(context, uri)

        return MovieEntity(
            title = title,
            artists = "",
            releaseYear = 0,
            parentalRating = "",
            thumbnailUri = poster,
            description = "",
            starRating = 0,
            videoPath = uri.toString(),
            uri = uri.toString()
        )
    }

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

package com.example.atuneslegacy.data.importing

import android.content.Context
import android.os.Environment
import com.example.atuneslegacy.data.local.entity.AudiobookChapter
import com.example.atuneslegacy.data.repository.MediaRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File

object ATunesLibrarySyncEngine {

    fun getLibraryDirectories(context: Context? = null): List<File> {
        val dirs = mutableSetOf<File>()

        // 1. Root of Internal Storage: /storage/emulated/0/aTunes Library/
        try {
            val rootLibDir = File(Environment.getExternalStorageDirectory(), "aTunes Library")
            rootLibDir.mkdirs()
            File(rootLibDir, "Audiobooks").mkdirs()
            File(rootLibDir, "Podcasts").mkdirs()
            File(rootLibDir, "TV-Shows").mkdirs()
            File(rootLibDir, "Movies").mkdirs()
            File(rootLibDir, "Shorts").mkdirs()
            File(rootLibDir, "Music/Albums").mkdirs()
            File(rootLibDir, "Music/Playlists").mkdirs()
            File(rootLibDir, "Music/Music Videos").mkdirs()
            if (rootLibDir.exists()) {
                dirs.add(rootLibDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Music Directory: /storage/emulated/0/Music/aTunes Library/
        try {
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val musicLibDir = File(musicDir, "aTunes Library")
            musicLibDir.mkdirs()
            File(musicLibDir, "Audiobooks").mkdirs()
            File(musicLibDir, "Podcasts").mkdirs()
            File(musicLibDir, "TV-Shows").mkdirs()
            File(musicLibDir, "Movies").mkdirs()
            File(musicLibDir, "Shorts").mkdirs()
            File(musicLibDir, "Music/Albums").mkdirs()
            File(musicLibDir, "Music/Playlists").mkdirs()
            File(musicLibDir, "Music/Music Videos").mkdirs()
            if (musicLibDir.exists()) {
                dirs.add(musicLibDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Download Directory: /storage/emulated/0/Download/aTunes Library/
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val downloadLibDir = File(downloadDir, "aTunes Library")
            if (downloadLibDir.exists()) {
                dirs.add(downloadLibDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. App-specific storage fallback
        if (context != null) {
            try {
                val extFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
                val appLibDir = File(extFilesDir, "aTunes Library")
                appLibDir.mkdirs()
                File(appLibDir, "Audiobooks").mkdirs()
                File(appLibDir, "Podcasts").mkdirs()
                File(appLibDir, "TV-Shows").mkdirs()
                File(appLibDir, "Movies").mkdirs()
                File(appLibDir, "Shorts").mkdirs()
                File(appLibDir, "Music/Albums").mkdirs()
                File(appLibDir, "Music/Playlists").mkdirs()
                File(appLibDir, "Music/Music Videos").mkdirs()
                if (appLibDir.exists()) {
                    dirs.add(appLibDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return dirs.toList().ifEmpty {
            listOf(File(Environment.getExternalStorageDirectory(), "aTunes Library").apply { mkdirs() })
        }
    }

    fun getMasterLibraryDir(context: Context? = null): File {
        return getLibraryDirectories(context).first()
    }

    suspend fun syncLibrary(context: Context, repository: MediaRepository) {
        val libDirs = getLibraryDirectories(context)
        MediaImportHelper.scanAndImportATunesDirectory(context, repository)

        try {
            for (libDir in libDirs) {
                // 1. Export Albums & Songs metadata
                val albums = repository.getAllAlbums().first()
                val albumsDir = File(libDir, "Music/Albums")
                if (albumsDir.exists()) {
                    for (album in albums) {
                        val albFolder = File(albumsDir, album.title.ifBlank { "Unspecified" })
                        if (!albFolder.exists()) albFolder.mkdirs()
                        val metaFile = File(albFolder, "Metadata.txt")
                        val songsInAlbum = repository.getSongsByAlbum(album.title).first()
                        val content = buildString {
                            appendLine("Title: \"${album.title}\"")
                            appendLine("Artist: \"${album.albumArtist}\"")
                            appendLine("Genre: \"${album.genre}\"")
                            appendLine("Year: ${album.releaseYear}")
                            appendLine("Songs:")
                            songsInAlbum.forEach { s ->
                                appendLine(" - Track ${s.trackNumber}: ${s.title} by ${s.artist} [Genre: ${s.genre}]")
                            }
                        }
                        metaFile.writeText(content)
                    }
                }

                // 2. Export Music Videos metadata
                val musicVideos = repository.getMusicVideos().first()
                val mvDir = File(libDir, "Music/Music Videos")
                if (mvDir.exists()) {
                    for (mv in musicVideos) {
                        val mvFolder = File(mvDir, mv.title.ifBlank { "Unspecified" })
                        if (!mvFolder.exists()) mvFolder.mkdirs()
                        val metaFile = File(mvFolder, "Metadata.txt")
                        val content = buildString {
                            appendLine("Title: \"${mv.title}\"")
                            appendLine("Artist: \"${mv.artist}\"")
                            appendLine("Album Title: \"${mv.albumTitle}\"")
                            appendLine("Genre: \"${mv.genre}\"")
                            appendLine("Year: ${mv.year}")
                        }
                        metaFile.writeText(content)
                    }
                }

                // 3. Export Movies metadata
                val movies = repository.getAllMovies().first()
                val moviesDir = File(libDir, "Movies")
                if (moviesDir.exists()) {
                    for (movie in movies) {
                        val movFolder = File(moviesDir, movie.title.ifBlank { "Unspecified" })
                        if (!movFolder.exists()) movFolder.mkdirs()
                        val metaFile = File(movFolder, "Metadata.txt")
                        val content = buildString {
                            appendLine("Title: \"${movie.title}\"")
                            appendLine("Parental Rating: \"${movie.parentalRating}\"")
                            appendLine("Rating: ${movie.starRating}")
                            appendLine("Show Description: \"${movie.description}\"")
                        }
                        metaFile.writeText(content)
                    }
                }

                // 4. Export TV Shows & Season Level Metadata.txt
                val tvShows = repository.getAllTvShows().first()
                val tvDir = File(libDir, "TV-Shows")
                if (tvDir.exists()) {
                    for (show in tvShows) {
                        val showFolder = File(tvDir, show.title.ifBlank { "Unspecified" })
                        if (!showFolder.exists()) showFolder.mkdirs()

                        val showMetaFile = File(showFolder, "Metadata.txt")
                        val showContent = buildString {
                            appendLine("Title: \"${show.title}\"")
                            appendLine("Parental Rating: \"${show.parentalRating}\"")
                            appendLine("Rating: ${show.starRating}")
                            appendLine("Show Description: \"${show.description}\"")
                        }
                        showMetaFile.writeText(showContent)

                        val episodes = repository.getEpisodesByShow(show.title).first()
                        val seasons = episodes.groupBy { it.seasonNumber }
                        for ((seasonNum, seasonEpisodes) in seasons) {
                            val seasonFolder = File(showFolder, "Season $seasonNum")
                            if (!seasonFolder.exists()) seasonFolder.mkdirs()

                            val seasonMetaFile = File(seasonFolder, "Metadata.txt")
                            val seasonContent = buildString {
                                appendLine("Folder: Internal storage/Music/aTunes Library/TV-Shows/${show.title}/${show.title} Season $seasonNum/")
                                appendLine("Season: $seasonNum")
                                appendLine("Media Files:")
                                seasonEpisodes.forEach { ep ->
                                    val fileName = File(ep.uri).name.ifBlank { "Episode ${ep.episodeNumber}.mp4" }
                                    appendLine(" - $fileName (\"${ep.title}\") ${ep.episodeNumber}")
                                }
                            }
                            seasonMetaFile.writeText(seasonContent)
                        }
                    }
                }

                // 5. Export Podcasts matching exact user format (Show Title, Host, Rating, Catagory, Description)
                val podcasts = repository.getAllPodcastShows().first()
                val podcastDir = File(libDir, "Podcasts")
                if (podcastDir.exists()) {
                    for (pod in podcasts) {
                        val podFolder = File(podcastDir, pod.title.ifBlank { "Unspecified" })
                        if (!podFolder.exists()) podFolder.mkdirs()

                        val showMetaFile = File(podFolder, "Metadata.txt")
                        val showContent = buildString {
                            appendLine("Show Title: \"${pod.title}\"")
                            appendLine("Host: \"${pod.creatorHost}\"")
                            appendLine("Rating: ${pod.starRating}")
                            appendLine("Catagory: \"${pod.category}\"")
                            appendLine("Description: \"${pod.description}\"")
                        }
                        showMetaFile.writeText(showContent)

                        val episodes = repository.getEpisodesByPodcast(pod.title).first()
                        val seasons = episodes.groupBy { it.seasonNumber }
                        for ((seasonNum, seasonEpisodes) in seasons) {
                            val seasonFolder = File(podFolder, "Season $seasonNum")
                            if (!seasonFolder.exists()) seasonFolder.mkdirs()

                            val seasonMetaFile = File(seasonFolder, "Metadata.txt")
                            val seasonContent = buildString {
                                appendLine("Folder: Internal storage/Music/aTunes Library/Podcasts/${pod.title}/${pod.title} Season $seasonNum/")
                                appendLine("Season: $seasonNum")
                                appendLine("Media Files:")
                                seasonEpisodes.forEach { ep ->
                                    val fileName = File(ep.uri).name.ifBlank { "Episode ${ep.episodeNumber}.mp3" }
                                    appendLine(" - $fileName (\"${ep.title}\") ${ep.episodeNumber}")
                                }
                            }
                            seasonMetaFile.writeText(seasonContent)
                        }
                    }
                }

                // 6. Export Audiobooks
                val audiobooks = repository.getAllAudiobooks().first()
                val audiobookDir = File(libDir, "Audiobooks")
                if (audiobookDir.exists()) {
                    for (book in audiobooks) {
                        val bookFolder = File(audiobookDir, book.title.ifBlank { "Unspecified" })
                        if (!bookFolder.exists()) bookFolder.mkdirs()
                        val metaFile = File(bookFolder, "Metadata.txt")
                        val content = buildString {
                            appendLine("Title: \"${book.title}\"")
                            appendLine("Author: \"${book.author}\"")
                            appendLine("Genre: \"${book.genre}\"")
                            appendLine("Release Year: \"${book.publicationYear}\"")
                            appendLine("Book Series: \"${book.bookSeries}\"")
                            if (book.description.isNotBlank()) {
                                appendLine("Description: \"${book.description}\"")
                            }
                            if (book.chaptersJson.isNotBlank()) {
                                appendLine()
                                appendLine("Chapters:")
                                try {
                                    val chapters = Json.decodeFromString<List<AudiobookChapter>>(book.chaptersJson)
                                    for (ch in chapters) {
                                        appendLine("${ch.time} - \"${ch.title}\" ${ch.number}")
                                    }
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        }
                        metaFile.writeText(content)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

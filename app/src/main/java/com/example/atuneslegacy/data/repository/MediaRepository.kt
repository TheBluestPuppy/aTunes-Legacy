package com.example.atuneslegacy.data.repository

import com.example.atuneslegacy.data.local.dao.AudiobookDao
import com.example.atuneslegacy.data.local.dao.MovieDao
import com.example.atuneslegacy.data.local.dao.MusicDao
import com.example.atuneslegacy.data.local.dao.PodcastDao
import com.example.atuneslegacy.data.local.dao.TvDao
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.PlaylistItemEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val musicDao: MusicDao,
    private val tvDao: TvDao,
    private val movieDao: MovieDao,
    private val podcastDao: PodcastDao,
    private val audiobookDao: AudiobookDao
) {
    // --- MUSIC & SONGS ---
    fun getAllSongs(): Flow<List<SongEntity>> = musicDao.getAllSongs()
    fun getMusicSongs(): Flow<List<SongEntity>> = musicDao.getMusicSongs()
    fun getMusicVideos(): Flow<List<SongEntity>> = musicDao.getMusicVideos()
    suspend fun getSongById(id: Long): SongEntity? = musicDao.getSongById(id)
    fun getSongsByAlbum(albumTitle: String): Flow<List<SongEntity>> = musicDao.getSongsByAlbum(albumTitle)
    suspend fun insertSong(song: SongEntity): Long = musicDao.insertSong(song)
    suspend fun insertSongs(songs: List<SongEntity>) = musicDao.insertSongs(songs)
    suspend fun updateSong(song: SongEntity) = musicDao.updateSong(song)
    suspend fun deleteSong(song: SongEntity) = musicDao.deleteSong(song)

    // --- ALBUMS & CASCADING UPDATE ---
    fun getAllAlbums(): Flow<List<AlbumEntity>> = musicDao.getAllAlbums()
    suspend fun getAlbumById(id: Long): AlbumEntity? = musicDao.getAlbumById(id)
    suspend fun getAlbumByTitle(title: String): AlbumEntity? = musicDao.getAlbumByTitle(title)
    suspend fun insertAlbum(album: AlbumEntity): Long = musicDao.insertAlbum(album)
    suspend fun updateAlbum(album: AlbumEntity) = musicDao.updateAlbum(album)
    suspend fun deleteAlbum(album: AlbumEntity) = musicDao.deleteAlbum(album)

    /**
     * Cascading update for Album:
     * When Album metadata is updated -> cascade update to all matching songs (albumTitle, albumArtist, genre, releaseYear).
     */
    suspend fun updateAlbumAndCascade(oldAlbumTitle: String, updatedAlbum: AlbumEntity) {
        musicDao.updateAlbum(updatedAlbum)
        musicDao.updateSongsAlbumMetadata(
            oldAlbumTitle = oldAlbumTitle,
            newAlbumTitle = updatedAlbum.title,
            newAlbumArtist = updatedAlbum.albumArtist,
            newGenre = updatedAlbum.genre,
            newReleaseYear = updatedAlbum.releaseYear
        )
    }

    // --- PLAYLISTS ---
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long = musicDao.insertPlaylist(playlist)
    suspend fun deletePlaylist(playlist: PlaylistEntity) = musicDao.deletePlaylist(playlist)
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = musicDao.getPlaylistItems(playlistId)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long = musicDao.insertPlaylistItem(item)
    suspend fun deletePlaylistItem(item: PlaylistItemEntity) = musicDao.deletePlaylistItem(item)
    suspend fun clearPlaylist(playlistId: Long) = musicDao.clearPlaylist(playlistId)

    // --- TV SHOWS & EPISODES & CASCADING UPDATE ---
    fun getAllTvShows(): Flow<List<TvShowEntity>> = tvDao.getAllTvShows()
    suspend fun getTvShowById(id: Long): TvShowEntity? = tvDao.getTvShowById(id)
    suspend fun insertTvShow(show: TvShowEntity): Long = tvDao.insertTvShow(show)
    suspend fun updateTvShow(show: TvShowEntity) = tvDao.updateTvShow(show)
    suspend fun deleteTvShow(show: TvShowEntity) = tvDao.deleteTvShow(show)

    fun getEpisodesByShow(showTitle: String): Flow<List<TvEpisodeEntity>> = tvDao.getEpisodesByShow(showTitle)
    fun getAllTvEpisodes(): Flow<List<TvEpisodeEntity>> = tvDao.getAllEpisodes()
    suspend fun getEpisodeById(id: Long): TvEpisodeEntity? = tvDao.getEpisodeById(id)
    suspend fun insertTvEpisode(episode: TvEpisodeEntity): Long = tvDao.insertEpisode(episode)
    suspend fun updateTvEpisode(episode: TvEpisodeEntity) = tvDao.updateEpisode(episode)
    suspend fun deleteTvEpisode(episode: TvEpisodeEntity) = tvDao.deleteEpisode(episode)

    /**
     * Cascading update for TV Show:
     * When TV Show metadata is updated -> cascade update to all matching episodes (showTitle).
     */
    suspend fun updateTvShowAndCascade(oldShowTitle: String, updatedTvShow: TvShowEntity) {
        tvDao.updateTvShow(updatedTvShow)
        tvDao.updateTvEpisodesShowTitle(
            oldShowTitle = oldShowTitle,
            newShowTitle = updatedTvShow.title
        )
    }

    // --- MOVIES ---
    fun getAllMovies(): Flow<List<MovieEntity>> = movieDao.getAllMovies()
    suspend fun getMovieById(id: Long): MovieEntity? = movieDao.getMovieById(id)
    suspend fun insertMovie(movie: MovieEntity): Long = movieDao.insertMovie(movie)
    suspend fun updateMovie(movie: MovieEntity) = movieDao.updateMovie(movie)
    suspend fun deleteMovie(movie: MovieEntity) = movieDao.deleteMovie(movie)

    // --- PODCAST SHOWS & EPISODES & CASCADING UPDATE ---
    fun getAllPodcastShows(): Flow<List<PodcastShowEntity>> = podcastDao.getAllPodcastShows()
    suspend fun getPodcastShowById(id: Long): PodcastShowEntity? = podcastDao.getPodcastShowById(id)
    suspend fun insertPodcastShow(show: PodcastShowEntity): Long = podcastDao.insertPodcastShow(show)
    suspend fun updatePodcastShow(show: PodcastShowEntity) = podcastDao.updatePodcastShow(show)
    suspend fun deletePodcastShow(show: PodcastShowEntity) = podcastDao.deletePodcastShow(show)

    fun getEpisodesByPodcast(podcastName: String): Flow<List<PodcastEpisodeEntity>> = podcastDao.getEpisodesByPodcast(podcastName)
    fun getAllPodcastEpisodes(): Flow<List<PodcastEpisodeEntity>> = podcastDao.getAllPodcastEpisodes()
    suspend fun getPodcastEpisodeById(id: Long): PodcastEpisodeEntity? = podcastDao.getPodcastEpisodeById(id)
    suspend fun insertPodcastEpisode(episode: PodcastEpisodeEntity): Long = podcastDao.insertPodcastEpisode(episode)
    suspend fun updatePodcastEpisode(episode: PodcastEpisodeEntity) = podcastDao.updatePodcastEpisode(episode)
    suspend fun deletePodcastEpisode(episode: PodcastEpisodeEntity) = podcastDao.deletePodcastEpisode(episode)

    /**
     * Cascading update for Podcast Show:
     * When Podcast Show metadata is updated -> cascade update to all matching episodes (podcastName).
     */
    suspend fun updatePodcastShowAndCascade(oldPodcastName: String, updatedPodcastShow: PodcastShowEntity) {
        podcastDao.updatePodcastShow(updatedPodcastShow)
        podcastDao.updatePodcastEpisodesPodcastName(
            oldPodcastName = oldPodcastName,
            newPodcastName = updatedPodcastShow.title
        )
    }

    // --- AUDIOBOOKS & BOOK SERIES & CASCADING UPDATE ---
    fun getAllAudiobooks(): Flow<List<AudiobookEntity>> = audiobookDao.getAllAudiobooks()
    suspend fun getAudiobookById(id: Long): AudiobookEntity? = audiobookDao.getAudiobookById(id)
    fun getAudiobooksBySeries(seriesTitle: String): Flow<List<AudiobookEntity>> = audiobookDao.getAudiobooksBySeries(seriesTitle)
    suspend fun insertAudiobook(audiobook: AudiobookEntity): Long = audiobookDao.insertAudiobook(audiobook)
    suspend fun updateAudiobook(audiobook: AudiobookEntity) = audiobookDao.updateAudiobook(audiobook)
    suspend fun deleteAudiobook(audiobook: AudiobookEntity) = audiobookDao.deleteAudiobook(audiobook)

    fun getAllBookSeries(): Flow<List<BookSeriesEntity>> = audiobookDao.getAllBookSeries()
    suspend fun getBookSeriesById(id: Long): BookSeriesEntity? = audiobookDao.getBookSeriesById(id)
    suspend fun insertBookSeries(series: BookSeriesEntity): Long = audiobookDao.insertBookSeries(series)
    suspend fun updateBookSeries(series: BookSeriesEntity) = audiobookDao.updateBookSeries(series)
    suspend fun deleteBookSeries(series: BookSeriesEntity) = audiobookDao.deleteBookSeries(series)

    /**
     * Cascading update for Book Series:
     * When Book Series metadata is updated -> cascade update to all matching books (bookSeries, genre).
     */
    suspend fun updateBookSeriesAndCascade(oldSeriesTitle: String, updatedSeries: BookSeriesEntity) {
        audiobookDao.updateBookSeries(updatedSeries)
        audiobookDao.updateAudiobooksSeriesMetadata(
            oldSeriesTitle = oldSeriesTitle,
            newSeriesTitle = updatedSeries.title,
            newGenre = updatedSeries.genre
        )
    }

    suspend fun clearAllMedia() {
        musicDao.deleteAllSongs()
        musicDao.deleteAllAlbums()
        musicDao.deleteAllPlaylists()
        musicDao.deleteAllPlaylistItems()
        tvDao.deleteAllTvShows()
        tvDao.deleteAllTvEpisodes()
        movieDao.deleteAllMovies()
        podcastDao.deleteAllPodcastShows()
        podcastDao.deleteAllPodcastEpisodes()
        audiobookDao.deleteAllAudiobooks()
        audiobookDao.deleteAllBookSeries()
    }
}

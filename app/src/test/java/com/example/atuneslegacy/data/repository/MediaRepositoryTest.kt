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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MediaRepositoryTest {

    private lateinit var repository: MediaRepository
    private lateinit var fakeMusicDao: FakeMusicDao
    private lateinit var fakeTvDao: FakeTvDao
    private lateinit var fakeMovieDao: FakeMovieDao
    private lateinit var fakePodcastDao: FakePodcastDao
    private lateinit var fakeAudiobookDao: FakeAudiobookDao

    @Before
    fun setUp() {
        fakeMusicDao = FakeMusicDao()
        fakeTvDao = FakeTvDao()
        fakeMovieDao = FakeMovieDao()
        fakePodcastDao = FakePodcastDao()
        fakeAudiobookDao = FakeAudiobookDao()

        repository = MediaRepository(
            musicDao = fakeMusicDao,
            tvDao = fakeTvDao,
            movieDao = fakeMovieDao,
            podcastDao = fakePodcastDao,
            audiobookDao = fakeAudiobookDao
        )
    }

    @Test
    fun testAlbumCascadeUpdate() = runTest {
        // Initial album and song
        val album = AlbumEntity(id = 1, title = "Original Album", albumArtist = "Original Artist", genre = "Rock", releaseYear = 2000)
        val song1 = SongEntity(id = 1, title = "Song 1", artist = "Artist 1", albumArtist = "Original Artist", albumTitle = "Original Album", genre = "Rock", year = 2000, uri = "uri1")
        val song2 = SongEntity(id = 2, title = "Song 2", artist = "Artist 2", albumArtist = "Original Artist", albumTitle = "Original Album", genre = "Rock", year = 2000, uri = "uri2")

        fakeMusicDao.insertAlbum(album)
        fakeMusicDao.insertSong(song1)
        fakeMusicDao.insertSong(song2)

        // Updated album metadata
        val updatedAlbum = AlbumEntity(id = 1, title = "Remastered Album", albumArtist = "New Artist", genre = "Pop Rock", releaseYear = 2024)
        repository.updateAlbumAndCascade(oldAlbumTitle = "Original Album", updatedAlbum = updatedAlbum)

        val updatedSong1 = fakeMusicDao.getSongById(1)
        val updatedSong2 = fakeMusicDao.getSongById(2)

        assertEquals("Remastered Album", updatedSong1?.albumTitle)
        assertEquals("New Artist", updatedSong1?.albumArtist)
        assertEquals("Pop Rock", updatedSong1?.genre)
        assertEquals(2024, updatedSong1?.year)

        assertEquals("Remastered Album", updatedSong2?.albumTitle)
        assertEquals("New Artist", updatedSong2?.albumArtist)
    }

    @Test
    fun testTvShowCascadeUpdate() = runTest {
        val show = TvShowEntity(id = 1, title = "Old Show Title", releaseYear = 2010)
        val episode1 = TvEpisodeEntity(id = 1, title = "Pilot", showTitle = "Old Show Title", seasonNumber = 1, episodeNumber = 1, uri = "uri1")

        fakeTvDao.insertTvShow(show)
        fakeTvDao.insertEpisode(episode1)

        val updatedShow = TvShowEntity(id = 1, title = "New Show Title", releaseYear = 2010)
        repository.updateTvShowAndCascade(oldShowTitle = "Old Show Title", updatedTvShow = updatedShow)

        val updatedEpisode = fakeTvDao.getEpisodeById(1)
        assertEquals("New Show Title", updatedEpisode?.showTitle)
    }

    @Test
    fun testPodcastShowCascadeUpdate() = runTest {
        val podcast = PodcastShowEntity(id = 1, title = "Old Podcast Name", creatorHost = "Host")
        val ep = PodcastEpisodeEntity(id = 1, title = "Ep 1", podcastName = "Old Podcast Name", uri = "uri1")

        fakePodcastDao.insertPodcastShow(podcast)
        fakePodcastDao.insertPodcastEpisode(ep)

        val updatedPodcast = PodcastShowEntity(id = 1, title = "New Podcast Name", creatorHost = "Host")
        repository.updatePodcastShowAndCascade(oldPodcastName = "Old Podcast Name", updatedPodcastShow = updatedPodcast)

        val updatedEp = fakePodcastDao.getPodcastEpisodeById(1)
        assertEquals("New Podcast Name", updatedEp?.podcastName)
    }

    @Test
    fun testBookSeriesCascadeUpdate() = runTest {
        val series = BookSeriesEntity(id = 1, title = "Old Series", genre = "Sci-Fi")
        val book = AudiobookEntity(id = 1, title = "Book 1", author = "Author", bookSeries = "Old Series", genre = "Sci-Fi", uri = "uri1")

        fakeAudiobookDao.insertBookSeries(series)
        fakeAudiobookDao.insertAudiobook(book)

        val updatedSeries = BookSeriesEntity(id = 1, title = "New Series", genre = "Fantasy")
        repository.updateBookSeriesAndCascade(oldSeriesTitle = "Old Series", updatedSeries = updatedSeries)

        val updatedBook = fakeAudiobookDao.getAudiobookById(1)
        assertEquals("New Series", updatedBook?.bookSeries)
        assertEquals("Fantasy", updatedBook?.genre)
    }
}

// Fake DAOs for unit testing
class FakeMusicDao : MusicDao {
    private val songsMap = mutableMapOf<Long, SongEntity>()
    private val albumsMap = mutableMapOf<Long, AlbumEntity>()
    private val songsFlow = MutableStateFlow<List<SongEntity>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())

    private fun notifyChanges() {
        songsFlow.value = songsMap.values.toList()
        albumsFlow.value = albumsMap.values.toList()
    }

    override fun getAllSongs(): Flow<List<SongEntity>> = songsFlow
    override fun getMusicSongs(): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { !it.isMusicVideo } }
    override fun getMusicVideos(): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { it.isMusicVideo } }
    override suspend fun getSongById(id: Long): SongEntity? = songsMap[id]
    override fun getSongsByAlbum(albumTitle: String): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { it.albumTitle == albumTitle } }

    override suspend fun insertSong(song: SongEntity): Long {
        val id = if (song.id == 0L) (songsMap.keys.maxOrNull() ?: 0L) + 1 else song.id
        val entity = song.copy(id = id)
        songsMap[id] = entity
        notifyChanges()
        return id
    }

    override suspend fun insertSongs(songs: List<SongEntity>) {
        songs.forEach { insertSong(it) }
    }

    override suspend fun updateSong(song: SongEntity) {
        songsMap[song.id] = song
        notifyChanges()
    }

    override suspend fun deleteSong(song: SongEntity) {
        songsMap.remove(song.id)
        notifyChanges()
    }

    override suspend fun deleteSongById(id: Long) {
        songsMap.remove(id)
        notifyChanges()
    }

    override suspend fun updateSongsAlbumMetadata(
        oldAlbumTitle: String,
        newAlbumTitle: String,
        newAlbumArtist: String,
        newGenre: String,
        newReleaseYear: Int
    ) {
        songsMap.entries.forEach { (id, song) ->
            if (song.albumTitle == oldAlbumTitle) {
                songsMap[id] = song.copy(
                    albumTitle = newAlbumTitle,
                    albumArtist = newAlbumArtist,
                    genre = newGenre,
                    year = newReleaseYear
                )
            }
        }
        notifyChanges()
    }

    override fun getAllAlbums(): Flow<List<AlbumEntity>> = albumsFlow
    override suspend fun getAlbumById(id: Long): AlbumEntity? = albumsMap[id]
    override suspend fun getAlbumByTitle(title: String): AlbumEntity? = albumsMap.values.firstOrNull { it.title == title }

    override suspend fun insertAlbum(album: AlbumEntity): Long {
        val id = if (album.id == 0L) (albumsMap.keys.maxOrNull() ?: 0L) + 1 else album.id
        val entity = album.copy(id = id)
        albumsMap[id] = entity
        notifyChanges()
        return id
    }

    override suspend fun updateAlbum(album: AlbumEntity) {
        albumsMap[album.id] = album
        notifyChanges()
    }

    override suspend fun deleteAlbum(album: AlbumEntity) {
        albumsMap.remove(album.id)
        notifyChanges()
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = MutableStateFlow(emptyList())
    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long = 1
    override suspend fun deletePlaylist(playlist: PlaylistEntity) {}
    override fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = MutableStateFlow(emptyList())
    override suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long = 1
    override suspend fun deletePlaylistItem(item: PlaylistItemEntity) {}
    override suspend fun clearPlaylist(playlistId: Long) {}
}

class FakeTvDao : TvDao {
    private val shows = mutableMapOf<Long, TvShowEntity>()
    private val episodes = mutableMapOf<Long, TvEpisodeEntity>()

    override fun getAllTvShows(): Flow<List<TvShowEntity>> = MutableStateFlow(shows.values.toList())
    override suspend fun getTvShowById(id: Long): TvShowEntity? = shows[id]
    override suspend fun getTvShowByTitle(title: String): TvShowEntity? = shows.values.firstOrNull { it.title == title }
    override suspend fun insertTvShow(show: TvShowEntity): Long {
        val id = if (show.id == 0L) (shows.keys.maxOrNull() ?: 0L) + 1 else show.id
        shows[id] = show.copy(id = id)
        return id
    }
    override suspend fun updateTvShow(show: TvShowEntity) { shows[show.id] = show }
    override suspend fun deleteTvShow(show: TvShowEntity) { shows.remove(show.id) }

    override fun getEpisodesByShow(showTitle: String): Flow<List<TvEpisodeEntity>> = MutableStateFlow(episodes.values.filter { it.showTitle == showTitle })
    override fun getAllEpisodes(): Flow<List<TvEpisodeEntity>> = MutableStateFlow(episodes.values.toList())
    override suspend fun getEpisodeById(id: Long): TvEpisodeEntity? = episodes[id]
    override suspend fun insertEpisode(episode: TvEpisodeEntity): Long {
        val id = if (episode.id == 0L) (episodes.keys.maxOrNull() ?: 0L) + 1 else episode.id
        episodes[id] = episode.copy(id = id)
        return id
    }
    override suspend fun updateEpisode(episode: TvEpisodeEntity) { episodes[episode.id] = episode }
    override suspend fun deleteEpisode(episode: TvEpisodeEntity) { episodes.remove(episode.id) }

    override suspend fun updateTvEpisodesShowTitle(oldShowTitle: String, newShowTitle: String) {
        episodes.entries.forEach { (id, ep) ->
            if (ep.showTitle == oldShowTitle) {
                episodes[id] = ep.copy(showTitle = newShowTitle)
            }
        }
    }
}

class FakeMovieDao : MovieDao {
    private val movies = mutableMapOf<Long, MovieEntity>()
    override fun getAllMovies(): Flow<List<MovieEntity>> = MutableStateFlow(movies.values.toList())
    override suspend fun getMovieById(id: Long): MovieEntity? = movies[id]
    override suspend fun insertMovie(movie: MovieEntity): Long {
        val id = if (movie.id == 0L) (movies.keys.maxOrNull() ?: 0L) + 1 else movie.id
        movies[id] = movie.copy(id = id)
        return id
    }
    override suspend fun updateMovie(movie: MovieEntity) { movies[movie.id] = movie }
    override suspend fun deleteMovie(movie: MovieEntity) { movies.remove(movie.id) }
    override suspend fun deleteMovieById(id: Long) { movies.remove(id) }
}

class FakePodcastDao : PodcastDao {
    private val shows = mutableMapOf<Long, PodcastShowEntity>()
    private val episodes = mutableMapOf<Long, PodcastEpisodeEntity>()

    override fun getAllPodcastShows(): Flow<List<PodcastShowEntity>> = MutableStateFlow(shows.values.toList())
    override suspend fun getPodcastShowById(id: Long): PodcastShowEntity? = shows[id]
    override suspend fun getPodcastShowByTitle(title: String): PodcastShowEntity? = shows.values.firstOrNull { it.title == title }
    override suspend fun insertPodcastShow(show: PodcastShowEntity): Long {
        val id = if (show.id == 0L) (shows.keys.maxOrNull() ?: 0L) + 1 else show.id
        shows[id] = show.copy(id = id)
        return id
    }
    override suspend fun updatePodcastShow(show: PodcastShowEntity) { shows[show.id] = show }
    override suspend fun deletePodcastShow(show: PodcastShowEntity) { shows.remove(show.id) }

    override fun getEpisodesByPodcast(podcastName: String): Flow<List<PodcastEpisodeEntity>> = MutableStateFlow(episodes.values.filter { it.podcastName == podcastName })
    override fun getAllPodcastEpisodes(): Flow<List<PodcastEpisodeEntity>> = MutableStateFlow(episodes.values.toList())
    override suspend fun getPodcastEpisodeById(id: Long): PodcastEpisodeEntity? = episodes[id]
    override suspend fun insertPodcastEpisode(episode: PodcastEpisodeEntity): Long {
        val id = if (episode.id == 0L) (episodes.keys.maxOrNull() ?: 0L) + 1 else episode.id
        episodes[id] = episode.copy(id = id)
        return id
    }
    override suspend fun updatePodcastEpisode(episode: PodcastEpisodeEntity) { episodes[episode.id] = episode }
    override suspend fun deletePodcastEpisode(episode: PodcastEpisodeEntity) { episodes.remove(episode.id) }

    override suspend fun updatePodcastEpisodesPodcastName(oldPodcastName: String, newPodcastName: String) {
        episodes.entries.forEach { (id, ep) ->
            if (ep.podcastName == oldPodcastName) {
                episodes[id] = ep.copy(podcastName = newPodcastName)
            }
        }
    }
}

class FakeAudiobookDao : AudiobookDao {
    private val books = mutableMapOf<Long, AudiobookEntity>()
    private val series = mutableMapOf<Long, BookSeriesEntity>()

    override fun getAllAudiobooks(): Flow<List<AudiobookEntity>> = MutableStateFlow(books.values.toList())
    override suspend fun getAudiobookById(id: Long): AudiobookEntity? = books[id]
    override fun getAudiobooksBySeries(seriesTitle: String): Flow<List<AudiobookEntity>> = MutableStateFlow(books.values.filter { it.bookSeries == seriesTitle })
    override suspend fun insertAudiobook(audiobook: AudiobookEntity): Long {
        val id = if (audiobook.id == 0L) (books.keys.maxOrNull() ?: 0L) + 1 else audiobook.id
        books[id] = audiobook.copy(id = id)
        return id
    }
    override suspend fun updateAudiobook(audiobook: AudiobookEntity) { books[audiobook.id] = audiobook }
    override suspend fun deleteAudiobook(audiobook: AudiobookEntity) { books.remove(audiobook.id) }

    override fun getAllBookSeries(): Flow<List<BookSeriesEntity>> = MutableStateFlow(series.values.toList())
    override suspend fun getBookSeriesById(id: Long): BookSeriesEntity? = series[id]
    override suspend fun getBookSeriesByTitle(title: String): BookSeriesEntity? = series.values.firstOrNull { it.title == title }
    override suspend fun insertBookSeries(s: BookSeriesEntity): Long {
        val id = if (s.id == 0L) (series.keys.maxOrNull() ?: 0L) + 1 else s.id
        series[id] = s.copy(id = id)
        return id
    }
    override suspend fun updateBookSeries(s: BookSeriesEntity) { series[s.id] = s }
    override suspend fun deleteBookSeries(s: BookSeriesEntity) { series.remove(s.id) }

    override suspend fun updateAudiobooksSeriesMetadata(
        oldSeriesTitle: String,
        newSeriesTitle: String,
        newGenre: String
    ) {
        books.entries.forEach { (id, book) ->
            if (book.bookSeries == oldSeriesTitle) {
                books[id] = book.copy(bookSeries = newSeriesTitle, genre = newGenre)
            }
        }
    }
}

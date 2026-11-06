package com.example.atuneslegacy.data.repository

import com.example.atuneslegacy.data.local.dao.AudiobookDao
import com.example.atuneslegacy.data.local.dao.MovieDao
import com.example.atuneslegacy.data.local.dao.MusicDao
import com.example.atuneslegacy.data.local.dao.PodcastDao
import com.example.atuneslegacy.data.local.dao.ShortsDao
import com.example.atuneslegacy.data.local.dao.TvDao
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.ArtistEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.GameShortcutEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.PlaylistItemEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.ShortVideoEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var fakeShortsDao: FakeShortsDao

    @Before
    fun setUp() {
        fakeMusicDao = FakeMusicDao()
        fakeTvDao = FakeTvDao()
        fakeMovieDao = FakeMovieDao()
        fakePodcastDao = FakePodcastDao()
        fakeAudiobookDao = FakeAudiobookDao()
        fakeShortsDao = FakeShortsDao()

        repository = MediaRepository(
            musicDao = fakeMusicDao,
            tvDao = fakeTvDao,
            movieDao = fakeMovieDao,
            podcastDao = fakePodcastDao,
            audiobookDao = fakeAudiobookDao,
            shortsDao = fakeShortsDao
        )
    }

    @Test
    fun testAlbumCascadeUpdate() = runTest {
        val album = AlbumEntity(id = 1, title = "Original Album", albumArtist = "Original Artist", genre = "Rock", releaseYear = 2000)
        val song1 = SongEntity(id = 1, title = "Song 1", artist = "Artist 1", albumArtist = "Original Artist", albumTitle = "Original Album", genre = "Rock", year = 2000, uri = "uri1")
        val song2 = SongEntity(id = 2, title = "Song 2", artist = "Artist 2", albumArtist = "Original Artist", albumTitle = "Original Album", genre = "Rock", year = 2000, uri = "uri2")

        fakeMusicDao.insertAlbum(album)
        fakeMusicDao.insertSong(song1)
        fakeMusicDao.insertSong(song2)

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
}

class FakeMusicDao : MusicDao {
    private val songsMap = mutableMapOf<Long, SongEntity>()
    private val albumsMap = mutableMapOf<Long, AlbumEntity>()
    private val artistsMap = mutableMapOf<String, ArtistEntity>()
    private val gameShortcutsMap = mutableMapOf<String, GameShortcutEntity>()
    private val playlistsMap = mutableMapOf<Long, PlaylistEntity>()
    private val playlistItemsMap = mutableMapOf<Long, PlaylistItemEntity>()

    private val songsFlow = MutableStateFlow<List<SongEntity>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())
    private val artistsFlow = MutableStateFlow<List<ArtistEntity>>(emptyList())
    private val gameShortcutsFlow = MutableStateFlow<List<GameShortcutEntity>>(emptyList())
    private val playlistsFlow = MutableStateFlow<List<PlaylistEntity>>(emptyList())

    private fun notifyChanges() {
        songsFlow.value = songsMap.values.toList()
        albumsFlow.value = albumsMap.values.toList()
        artistsFlow.value = artistsMap.values.toList()
        gameShortcutsFlow.value = gameShortcutsMap.values.toList()
        playlistsFlow.value = playlistsMap.values.toList()
    }

    override fun getAllSongs(): Flow<List<SongEntity>> = songsFlow
    override fun getMusicSongs(): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { !it.isMusicVideo } }
    override fun getMusicVideos(): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { it.isMusicVideo } }
    override suspend fun getSongById(id: Long): SongEntity? = songsMap[id]
    override suspend fun getSongByFilePath(filePath: String): SongEntity? = songsMap.values.firstOrNull { it.filePath == filePath || it.uri == filePath }
    override fun getSongsByAlbum(albumTitle: String): Flow<List<SongEntity>> = songsFlow.map { list -> list.filter { it.albumTitle == albumTitle } }

    override suspend fun insertSong(song: SongEntity): Long {
        val id = if (song.id == 0L) (songsMap.keys.maxOrNull() ?: 0L) + 1 else song.id
        songsMap[id] = song.copy(id = id)
        notifyChanges()
        return id
    }
    override suspend fun insertSongs(songs: List<SongEntity>) { songs.forEach { insertSong(it) } }
    override suspend fun updateSong(song: SongEntity) { songsMap[song.id] = song; notifyChanges() }
    override suspend fun deleteSong(song: SongEntity) { songsMap.remove(song.id); notifyChanges() }
    override suspend fun deleteSongById(id: Long) { songsMap.remove(id); notifyChanges() }

    override suspend fun updateSongsAlbumMetadata(oldAlbumTitle: String, newAlbumTitle: String, newAlbumArtist: String, newGenre: String, newReleaseYear: Int) {
        for ((id, song) in songsMap) {
            if (song.albumTitle.equals(oldAlbumTitle, ignoreCase = true)) {
                songsMap[id] = song.copy(albumTitle = newAlbumTitle, albumArtist = newAlbumArtist, genre = newGenre, year = newReleaseYear)
            }
        }
        notifyChanges()
    }

    override fun getAllAlbums(): Flow<List<AlbumEntity>> = albumsFlow
    override suspend fun getAlbumById(id: Long): AlbumEntity? = albumsMap[id]
    override suspend fun getAlbumByTitle(title: String): AlbumEntity? = albumsMap.values.firstOrNull { it.title.equals(title, ignoreCase = true) }
    override suspend fun insertAlbum(album: AlbumEntity): Long {
        val id = if (album.id == 0L) (albumsMap.keys.maxOrNull() ?: 0L) + 1 else album.id
        albumsMap[id] = album.copy(id = id)
        notifyChanges()
        return id
    }
    override suspend fun updateAlbum(album: AlbumEntity) { albumsMap[album.id] = album; notifyChanges() }
    override suspend fun deleteAlbum(album: AlbumEntity) { albumsMap.remove(album.id); notifyChanges() }

    override fun getAllArtists(): Flow<List<ArtistEntity>> = artistsFlow
    override suspend fun getArtistByName(name: String): ArtistEntity? = artistsMap[name]
    override suspend fun insertArtist(artist: ArtistEntity) { artistsMap[artist.name] = artist; notifyChanges() }
    override suspend fun updateArtistSongsMetadata(oldName: String, newName: String) {
        for ((id, song) in songsMap) {
            if (song.artist.equals(oldName, ignoreCase = true) || song.albumArtist.equals(oldName, ignoreCase = true)) {
                songsMap[id] = song.copy(artist = newName, albumArtist = newName)
            }
        }
        notifyChanges()
    }
    override suspend fun updateArtistAlbumsMetadata(oldName: String, newName: String) {
        for ((id, album) in albumsMap) {
            if (album.albumArtist.equals(oldName, ignoreCase = true)) {
                albumsMap[id] = album.copy(albumArtist = newName)
            }
        }
        notifyChanges()
    }

    override fun getAllGameShortcuts(): Flow<List<GameShortcutEntity>> = gameShortcutsFlow
    override suspend fun insertGameShortcut(shortcut: GameShortcutEntity) { gameShortcutsMap[shortcut.packageName] = shortcut; notifyChanges() }
    override suspend fun deleteGameShortcut(packageName: String) { gameShortcutsMap.remove(packageName); notifyChanges() }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistsFlow
    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long {
        val id = if (playlist.id == 0L) (playlistsMap.keys.maxOrNull() ?: 0L) + 1 else playlist.id
        playlistsMap[id] = playlist.copy(id = id)
        notifyChanges()
        return id
    }
    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistsMap[playlist.id] = playlist
        notifyChanges()
    }
    override suspend fun deletePlaylist(playlist: PlaylistEntity) { playlistsMap.remove(playlist.id); notifyChanges() }
    override fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = MutableStateFlow(playlistItemsMap.values.filter { it.playlistId == playlistId })
    override suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long {
        val id = if (item.id == 0L) (playlistItemsMap.keys.maxOrNull() ?: 0L) + 1 else item.id
        playlistItemsMap[id] = item.copy(id = id)
        return id
    }
    override suspend fun deletePlaylistItem(item: PlaylistItemEntity) { playlistItemsMap.remove(item.id) }
    override suspend fun clearPlaylist(playlistId: Long) { playlistItemsMap.values.removeAll { it.playlistId == playlistId } }

    override suspend fun deleteAllSongs() { songsMap.clear(); notifyChanges() }
    override suspend fun deleteAllAlbums() { albumsMap.clear(); notifyChanges() }
    override suspend fun deleteAllPlaylists() { playlistsMap.clear(); notifyChanges() }
    override suspend fun deleteAllPlaylistItems() { playlistItemsMap.clear() }
}

class FakeTvDao : TvDao {
    private val showsMap = mutableMapOf<Long, TvShowEntity>()
    private val episodesMap = mutableMapOf<Long, TvEpisodeEntity>()
    override fun getAllTvShows(): Flow<List<TvShowEntity>> = MutableStateFlow(emptyList())
    override suspend fun getTvShowById(id: Long): TvShowEntity? = showsMap[id]
    override suspend fun getTvShowByTitle(title: String): TvShowEntity? = null
    override suspend fun insertTvShow(show: TvShowEntity): Long = 1L
    override suspend fun updateTvShow(show: TvShowEntity) {}
    override suspend fun deleteTvShow(show: TvShowEntity) {}
    override fun getEpisodesByShow(showTitle: String): Flow<List<TvEpisodeEntity>> = MutableStateFlow(emptyList())
    override fun getAllEpisodes(): Flow<List<TvEpisodeEntity>> = MutableStateFlow(emptyList())
    override suspend fun getEpisodeById(id: Long): TvEpisodeEntity? = episodesMap[id]
    override suspend fun insertEpisode(episode: TvEpisodeEntity): Long = 1L
    override suspend fun updateEpisode(episode: TvEpisodeEntity) {}
    override suspend fun deleteEpisode(episode: TvEpisodeEntity) {}
    override suspend fun updateTvEpisodesShowTitle(oldShowTitle: String, newShowTitle: String) {}
    override suspend fun deleteAllTvShows() {}
    override suspend fun deleteAllTvEpisodes() {}
}

class FakeMovieDao : MovieDao {
    override fun getAllMovies(): Flow<List<MovieEntity>> = MutableStateFlow(emptyList())
    override suspend fun getMovieById(id: Long): MovieEntity? = null
    override suspend fun insertMovie(movie: MovieEntity): Long = 1L
    override suspend fun updateMovie(movie: MovieEntity) {}
    override suspend fun deleteMovie(movie: MovieEntity) {}
    override suspend fun deleteMovieById(id: Long) {}
    override suspend fun deleteAllMovies() {}
}

class FakePodcastDao : PodcastDao {
    override fun getAllPodcastShows(): Flow<List<PodcastShowEntity>> = MutableStateFlow(emptyList())
    override suspend fun getPodcastShowById(id: Long): PodcastShowEntity? = null
    override suspend fun getPodcastShowByTitle(title: String): PodcastShowEntity? = null
    override suspend fun insertPodcastShow(show: PodcastShowEntity): Long = 1L
    override suspend fun updatePodcastShow(show: PodcastShowEntity) {}
    override suspend fun deletePodcastShow(show: PodcastShowEntity) {}
    override fun getEpisodesByPodcast(podcastName: String): Flow<List<PodcastEpisodeEntity>> = MutableStateFlow(emptyList())
    override fun getAllPodcastEpisodes(): Flow<List<PodcastEpisodeEntity>> = MutableStateFlow(emptyList())
    override suspend fun getPodcastEpisodeById(id: Long): PodcastEpisodeEntity? = null
    override suspend fun insertPodcastEpisode(episode: PodcastEpisodeEntity): Long = 1L
    override suspend fun updatePodcastEpisode(episode: PodcastEpisodeEntity) {}
    override suspend fun deletePodcastEpisode(episode: PodcastEpisodeEntity) {}
    override suspend fun updatePodcastEpisodesPodcastName(oldPodcastName: String, newPodcastName: String) {}
    override suspend fun deleteAllPodcastShows() {}
    override suspend fun deleteAllPodcastEpisodes() {}
}

class FakeAudiobookDao : AudiobookDao {
    override fun getAllAudiobooks(): Flow<List<AudiobookEntity>> = MutableStateFlow(emptyList())
    override suspend fun getAudiobookById(id: Long): AudiobookEntity? = null
    override fun getAudiobooksBySeries(seriesTitle: String): Flow<List<AudiobookEntity>> = MutableStateFlow(emptyList())
    override suspend fun insertAudiobook(audiobook: AudiobookEntity): Long = 1L
    override suspend fun updateAudiobook(audiobook: AudiobookEntity) {}
    override suspend fun updateAudiobookPosition(id: Long, positionMs: Long) {}
    override suspend fun deleteAudiobook(audiobook: AudiobookEntity) {}
    override fun getAllBookSeries(): Flow<List<BookSeriesEntity>> = MutableStateFlow(emptyList())
    override suspend fun getBookSeriesById(id: Long): BookSeriesEntity? = null
    override suspend fun getBookSeriesByTitle(title: String): BookSeriesEntity? = null
    override suspend fun insertBookSeries(series: BookSeriesEntity): Long = 1L
    override suspend fun updateBookSeries(series: BookSeriesEntity) {}
    override suspend fun deleteBookSeries(series: BookSeriesEntity) {}
    override suspend fun updateAudiobooksSeriesMetadata(oldSeriesTitle: String, newSeriesTitle: String, newGenre: String) {}
    override suspend fun deleteAllAudiobooks() {}
    override suspend fun deleteAllBookSeries() {}
}

class FakeShortsDao : ShortsDao {
    private val shortsMap = mutableMapOf<Long, ShortVideoEntity>()
    private val shortsFlow = MutableStateFlow<List<ShortVideoEntity>>(emptyList())

    override fun getAllShorts(): Flow<List<ShortVideoEntity>> = shortsFlow
    override fun getSavedShorts(): Flow<List<ShortVideoEntity>> = shortsFlow.map { list -> list.filter { it.isSaved } }
    override suspend fun getShortByUri(uri: String): ShortVideoEntity? = shortsMap.values.firstOrNull { it.uri == uri || it.filePath == uri }
    override suspend fun insertShort(short: ShortVideoEntity): Long {
        val id = if (short.id == 0L) (shortsMap.keys.maxOrNull() ?: 0L) + 1 else short.id
        shortsMap[id] = short.copy(id = id)
        shortsFlow.value = shortsMap.values.toList()
        return id
    }
    override suspend fun updateShort(short: ShortVideoEntity) { shortsMap[short.id] = short; shortsFlow.value = shortsMap.values.toList() }
    override suspend fun deleteShort(short: ShortVideoEntity) { shortsMap.remove(short.id); shortsFlow.value = shortsMap.values.toList() }
    override suspend fun deleteAllShorts() { shortsMap.clear(); shortsFlow.value = emptyList() }
}

package com.example.atuneslegacy.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.atuneslegacy.data.auth.AuthHelper
import com.example.atuneslegacy.data.importing.ImportCategory
import com.example.atuneslegacy.data.importing.MediaImportHelper
import com.example.atuneslegacy.data.local.AppDatabase
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
import com.example.atuneslegacy.data.repository.MediaRepository
import com.example.atuneslegacy.player.MediaItemMetadata
import com.example.atuneslegacy.player.MediaPlayerManager
import com.example.atuneslegacy.player.MediaType
import com.example.atuneslegacy.ui.navigation.AudiobookSubTab
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.navigation.PodcastSubTab
import com.example.atuneslegacy.ui.theme.AppTheme
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val mediaRepository = MediaRepository(
        musicDao = db.musicDao(),
        tvDao = db.tvDao(),
        movieDao = db.movieDao(),
        podcastDao = db.podcastDao(),
        audiobookDao = db.audiobookDao()
    )
    val authHelper = AuthHelper()
    val mediaPlayerManager = MediaPlayerManager(application)

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.MUSIC)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Theme State
    private val _currentTheme = MutableStateFlow(AppTheme.LEGACY_LIGHT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    // Profile & Listening Stats State
    private val _profilePictureUri = MutableStateFlow<String?>(null)
    val profilePictureUri: StateFlow<String?> = _profilePictureUri.asStateFlow()

    fun setProfilePictureUri(uri: String?) {
        _profilePictureUri.value = uri
    }

    private val _listeningTimeMinutes = MutableStateFlow(320L) // 320 mins (~5.3 hours) listening time
    val listeningTimeMinutes: StateFlow<Long> = _listeningTimeMinutes.asStateFlow()

    private val _showProfilePage = MutableStateFlow(false)
    val showProfilePage: StateFlow<Boolean> = _showProfilePage.asStateFlow()

    fun setShowProfilePage(show: Boolean) {
        _showProfilePage.value = show
    }

    private val _userName = MutableStateFlow("Vinyl Collector")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBio = MutableStateFlow("Passionate audiophile and local media curator.")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    fun updateUserProfile(name: String, bio: String) {
        _userName.value = name.trim()
        _userBio.value = bio.trim()
    }

    private val _removedGamePackages = MutableStateFlow<Set<String>>(emptySet())
    val removedGamePackages: StateFlow<Set<String>> = _removedGamePackages.asStateFlow()

    fun removeGameShortcut(packageName: String) {
        _removedGamePackages.value = _removedGamePackages.value + packageName
    }

    private val _musicSubTab = MutableStateFlow(MusicSubTab.SONGS)
    val musicSubTab: StateFlow<MusicSubTab> = _musicSubTab.asStateFlow()

    private val _podcastSubTab = MutableStateFlow(PodcastSubTab.SHOWS)
    val podcastSubTab: StateFlow<PodcastSubTab> = _podcastSubTab.asStateFlow()

    private val _audiobookSubTab = MutableStateFlow(AudiobookSubTab.BOOKS)
    val audiobookSubTab: StateFlow<AudiobookSubTab> = _audiobookSubTab.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selection States for Detail Pages
    private val _selectedTvShow = MutableStateFlow<TvShowEntity?>(null)
    val selectedTvShow: StateFlow<TvShowEntity?> = _selectedTvShow.asStateFlow()

    private val _selectedPodcastShow = MutableStateFlow<PodcastShowEntity?>(null)
    val selectedPodcastShow: StateFlow<PodcastShowEntity?> = _selectedPodcastShow.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumEntity?>(null)
    val selectedAlbum: StateFlow<AlbumEntity?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist: StateFlow<String?> = _selectedArtist.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _selectedPodcastCategory = MutableStateFlow<String?>(null)
    val selectedPodcastCategory: StateFlow<String?> = _selectedPodcastCategory.asStateFlow()

    // Dialog & UI Modal States
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showAddMediaDialog = MutableStateFlow(false)
    val showAddMediaDialog: StateFlow<Boolean> = _showAddMediaDialog.asStateFlow()

    // Editing Dialog States
    private val _editingSong = MutableStateFlow<SongEntity?>(null)
    val editingSong: StateFlow<SongEntity?> = _editingSong.asStateFlow()

    private val _editingAlbum = MutableStateFlow<AlbumEntity?>(null)
    val editingAlbum: StateFlow<AlbumEntity?> = _editingAlbum.asStateFlow()

    private val _editingTvShow = MutableStateFlow<TvShowEntity?>(null)
    val editingTvShow: StateFlow<TvShowEntity?> = _editingTvShow.asStateFlow()

    private val _editingTvEpisode = MutableStateFlow<TvEpisodeEntity?>(null)
    val editingTvEpisode: StateFlow<TvEpisodeEntity?> = _editingTvEpisode.asStateFlow()

    private val _editingMovie = MutableStateFlow<MovieEntity?>(null)
    val editingMovie: StateFlow<MovieEntity?> = _editingMovie.asStateFlow()

    private val _editingPodcastShow = MutableStateFlow<PodcastShowEntity?>(null)
    val editingPodcastShow: StateFlow<PodcastShowEntity?> = _editingPodcastShow.asStateFlow()

    private val _editingPodcastEpisode = MutableStateFlow<PodcastEpisodeEntity?>(null)
    val editingPodcastEpisode: StateFlow<PodcastEpisodeEntity?> = _editingPodcastEpisode.asStateFlow()

    private val _editingAudiobook = MutableStateFlow<AudiobookEntity?>(null)
    val editingAudiobook: StateFlow<AudiobookEntity?> = _editingAudiobook.asStateFlow()

    private val _editingBookSeries = MutableStateFlow<BookSeriesEntity?>(null)
    val editingBookSeries: StateFlow<BookSeriesEntity?> = _editingBookSeries.asStateFlow()

    // Movie Grid/List View Toggle
    private val _isMovieGridView = MutableStateFlow(true)
    val isMovieGridView: StateFlow<Boolean> = _isMovieGridView.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isLoadingAuth = MutableStateFlow(false)
    val isLoadingAuth: StateFlow<Boolean> = _isLoadingAuth.asStateFlow()

    // --- DATA STREAMS FROM REPOSITORY ---
    val allSongs: StateFlow<List<SongEntity>> = combine(
        mediaRepository.getAllSongs(),
        _searchQuery
    ) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.albumTitle.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicSongs: StateFlow<List<SongEntity>> = combine(
        mediaRepository.getMusicSongs(),
        _searchQuery
    ) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.albumTitle.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicVideos: StateFlow<List<SongEntity>> = combine(
        mediaRepository.getMusicVideos(),
        _searchQuery
    ) { videos, query ->
        if (query.isBlank()) videos
        else videos.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<AlbumEntity>> = combine(
        mediaRepository.getAllAlbums(),
        _searchQuery
    ) { albums, query ->
        if (query.isBlank()) albums
        else albums.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.albumArtist.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = mediaRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTvShows: StateFlow<List<TvShowEntity>> = combine(
        mediaRepository.getAllTvShows(),
        _searchQuery
    ) { shows, query ->
        if (query.isBlank()) shows
        else shows.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tvEpisodesForSelectedShow: StateFlow<List<TvEpisodeEntity>> = combine(
        mediaRepository.getAllTvEpisodes(),
        _selectedTvShow
    ) { episodes, selectedShow ->
        if (selectedShow == null) emptyList()
        else episodes.filter { it.showTitle.equals(selectedShow.title, ignoreCase = true) }
            .sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovies: StateFlow<List<MovieEntity>> = combine(
        mediaRepository.getAllMovies(),
        _searchQuery
    ) { movies, query ->
        if (query.isBlank()) movies
        else movies.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artists.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPodcastShows: StateFlow<List<PodcastShowEntity>> = combine(
        mediaRepository.getAllPodcastShows(),
        _searchQuery,
        _selectedPodcastCategory
    ) { shows, query, category ->
        shows.filter { show ->
            val matchesQuery = query.isBlank() || show.title.contains(query, ignoreCase = true) || show.creatorHost.contains(query, ignoreCase = true)
            val matchesCategory = category.isNullOrBlank() || show.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val podcastEpisodesForSelectedShow: StateFlow<List<PodcastEpisodeEntity>> = combine(
        mediaRepository.getAllPodcastEpisodes(),
        _selectedPodcastShow
    ) { episodes, selectedShow ->
        if (selectedShow == null) emptyList()
        else episodes.filter { it.podcastName.equals(selectedShow.title, ignoreCase = true) }
            .sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAudiobooks: StateFlow<List<AudiobookEntity>> = combine(
        mediaRepository.getAllAudiobooks(),
        _searchQuery
    ) { books, query ->
        if (query.isBlank()) books
        else books.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookSeries: StateFlow<List<BookSeriesEntity>> = mediaRepository.getAllBookSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkUserAuth()
        seedSampleDataIfDatabaseEmpty()
    }

    // --- USER INTERACTION HANDLERS ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectMusicSubTab(subTab: MusicSubTab) {
        _musicSubTab.value = subTab
        _selectedAlbum.value = null
        _selectedArtist.value = null
        _selectedGenre.value = null
    }

    fun selectPodcastSubTab(subTab: PodcastSubTab) {
        _podcastSubTab.value = subTab
        if (subTab == PodcastSubTab.SHOWS) {
            _selectedPodcastCategory.value = null
        }
    }

    fun selectAudiobookSubTab(subTab: AudiobookSubTab) {
        _audiobookSubTab.value = subTab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTvShow(show: TvShowEntity?) {
        _selectedTvShow.value = show
    }

    fun selectPodcastShow(show: PodcastShowEntity?) {
        _selectedPodcastShow.value = show
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun selectAlbum(album: AlbumEntity?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    fun selectPodcastCategory(category: String?) {
        _selectedPodcastCategory.value = category
    }

    fun toggleMovieGridView() {
        _isMovieGridView.value = !_isMovieGridView.value
    }

    fun setShowCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setShowAddMediaDialog(show: Boolean) {
        _showAddMediaDialog.value = show
    }

    fun setEditingSong(song: SongEntity?) { _editingSong.value = song }
    fun setEditingAlbum(album: AlbumEntity?) { _editingAlbum.value = album }
    fun setEditingTvShow(show: TvShowEntity?) { _editingTvShow.value = show }
    fun setEditingTvEpisode(episode: TvEpisodeEntity?) { _editingTvEpisode.value = episode }
    fun setEditingMovie(movie: MovieEntity?) { _editingMovie.value = movie }
    fun setEditingPodcastShow(show: PodcastShowEntity?) { _editingPodcastShow.value = show }
    fun setEditingPodcastEpisode(episode: PodcastEpisodeEntity?) { _editingPodcastEpisode.value = episode }
    fun setEditingAudiobook(book: AudiobookEntity?) { _editingAudiobook.value = book }
    fun setEditingBookSeries(series: BookSeriesEntity?) { _editingBookSeries.value = series }

    fun updateSong(song: SongEntity) {
        viewModelScope.launch {
            mediaRepository.updateSong(song)
            _editingSong.value = null
        }
    }

    fun updateAlbum(oldTitle: String, album: AlbumEntity) {
        viewModelScope.launch {
            mediaRepository.updateAlbumAndCascade(oldTitle, album)
            _editingAlbum.value = null
        }
    }

    fun updateTvShow(oldTitle: String, show: TvShowEntity) {
        viewModelScope.launch {
            mediaRepository.updateTvShowAndCascade(oldTitle, show)
            _editingTvShow.value = null
        }
    }

    fun updateTvEpisode(episode: TvEpisodeEntity) {
        viewModelScope.launch {
            mediaRepository.updateTvEpisode(episode)
            _editingTvEpisode.value = null
        }
    }

    fun updateMovie(movie: MovieEntity) {
        viewModelScope.launch {
            mediaRepository.updateMovie(movie)
            _editingMovie.value = null
        }
    }

    fun updatePodcastShow(oldName: String, show: PodcastShowEntity) {
        viewModelScope.launch {
            mediaRepository.updatePodcastShowAndCascade(oldName, show)
            _editingPodcastShow.value = null
        }
    }

    fun updatePodcastEpisode(episode: PodcastEpisodeEntity) {
        viewModelScope.launch {
            mediaRepository.updatePodcastEpisode(episode)
            _editingPodcastEpisode.value = null
        }
    }

    fun updateAudiobook(book: AudiobookEntity) {
        viewModelScope.launch {
            mediaRepository.updateAudiobook(book)
            _editingAudiobook.value = null
        }
    }

    fun updateBookSeries(oldTitle: String, series: BookSeriesEntity) {
        viewModelScope.launch {
            mediaRepository.updateBookSeriesAndCascade(oldTitle, series)
            _editingBookSeries.value = null
        }
    }

    // --- MEDIA PLAYBACK METHODS ---
    fun playSong(song: SongEntity, playlist: List<SongEntity> = listOf(song)) {
        val mediaPlaylist = playlist.map {
            MediaItemMetadata(
                id = "song_${it.id}",
                title = it.title,
                subtitle = it.artist,
                albumOrCategory = it.albumTitle,
                mediaUri = it.uri,
                artworkUri = it.artworkUri,
                isVideo = it.isMusicVideo,
                mediaType = if (it.isMusicVideo) MediaType.MUSIC_VIDEO else MediaType.MUSIC
            )
        }
        val target = mediaPlaylist.firstOrNull { it.id == "song_${song.id}" } ?: mediaPlaylist.first()
        mediaPlayerManager.playMedia(target, mediaPlaylist)
    }

    fun playMovie(movie: MovieEntity) {
        val mediaItem = MediaItemMetadata(
            id = "movie_${movie.id}",
            title = movie.title,
            subtitle = movie.artists,
            albumOrCategory = "${movie.releaseYear} • ${movie.parentalRating}",
            mediaUri = movie.uri,
            artworkUri = movie.thumbnailUri,
            isVideo = true,
            mediaType = MediaType.MOVIE
        )
        mediaPlayerManager.playMedia(mediaItem)
    }

    fun playTvEpisode(episode: TvEpisodeEntity) {
        val mediaItem = MediaItemMetadata(
            id = "tv_${episode.id}",
            title = episode.title,
            subtitle = "${episode.showTitle} (S${episode.seasonNumber}E${episode.episodeNumber})",
            albumOrCategory = episode.showTitle,
            mediaUri = episode.uri,
            artworkUri = episode.thumbnailUri,
            isVideo = true,
            mediaType = MediaType.TV_SHOW
        )
        mediaPlayerManager.playMedia(mediaItem)
    }

    fun playPodcastEpisode(episode: PodcastEpisodeEntity) {
        val mediaItem = MediaItemMetadata(
            id = "podcast_${episode.id}",
            title = episode.title,
            subtitle = episode.podcastName,
            albumOrCategory = episode.podcastName,
            mediaUri = episode.uri,
            artworkUri = "",
            isVideo = false,
            mediaType = MediaType.PODCAST
        )
        mediaPlayerManager.playMedia(mediaItem)
    }

    fun playAudiobook(book: AudiobookEntity) {
        val mediaItem = MediaItemMetadata(
            id = "book_${book.id}",
            title = book.title,
            subtitle = book.author,
            albumOrCategory = book.genre,
            mediaUri = book.uri,
            artworkUri = book.portraitCoverUri,
            isVideo = false,
            mediaType = MediaType.AUDIOBOOK
        )
        mediaPlayerManager.playMedia(mediaItem)
    }

    // --- MEDIA DELETION METHODS ---
    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            mediaRepository.deleteSong(song)
            if (_editingSong.value?.id == song.id) _editingSong.value = null
        }
    }

    fun deleteAlbum(album: AlbumEntity) {
        viewModelScope.launch {
            mediaRepository.deleteAlbum(album)
            if (_editingAlbum.value?.id == album.id) _editingAlbum.value = null
        }
    }

    fun deleteTvShow(show: TvShowEntity) {
        viewModelScope.launch {
            mediaRepository.deleteTvShow(show)
            if (_editingTvShow.value?.id == show.id) _editingTvShow.value = null
        }
    }

    fun deleteTvEpisode(episode: TvEpisodeEntity) {
        viewModelScope.launch {
            mediaRepository.deleteTvEpisode(episode)
            if (_editingTvEpisode.value?.id == episode.id) _editingTvEpisode.value = null
        }
    }

    fun deleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            mediaRepository.deleteMovie(movie)
            if (_editingMovie.value?.id == movie.id) _editingMovie.value = null
        }
    }

    fun deletePodcastShow(show: PodcastShowEntity) {
        viewModelScope.launch {
            mediaRepository.deletePodcastShow(show)
            if (_editingPodcastShow.value?.id == show.id) _editingPodcastShow.value = null
        }
    }

    fun deletePodcastEpisode(episode: PodcastEpisodeEntity) {
        viewModelScope.launch {
            mediaRepository.deletePodcastEpisode(episode)
            if (_editingPodcastEpisode.value?.id == episode.id) _editingPodcastEpisode.value = null
        }
    }

    fun deleteAudiobook(book: AudiobookEntity) {
        viewModelScope.launch {
            mediaRepository.deleteAudiobook(book)
            if (_editingAudiobook.value?.id == book.id) _editingAudiobook.value = null
        }
    }

    fun deleteBookSeries(series: BookSeriesEntity) {
        viewModelScope.launch {
            mediaRepository.deleteBookSeries(series)
            if (_editingBookSeries.value?.id == series.id) _editingBookSeries.value = null
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            mediaRepository.deletePlaylist(playlist)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayerManager.release()
    }

    fun createPlaylist(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            mediaRepository.insertPlaylist(PlaylistEntity(title = title.trim()))
            _showCreatePlaylistDialog.value = false
        }
    }

    // --- AUTHENTICATION METHODS ---
    fun checkUserAuth() {
        try {
            _currentUser.value = authHelper.getCurrentUser()
        } catch (e: Exception) {
            _currentUser.value = null
        }
    }

    fun signUpWithEmail(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authMessage.value = null
            try {
                authHelper.signUpWithEmail(emailInput, passwordInput)
                _currentUser.value = authHelper.getCurrentUser()
                _authMessage.value = "Sign up successful! Please check your email for confirmation."
            } catch (e: Exception) {
                _authMessage.value = "Sign up error: ${e.message}"
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signInWithEmail(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authMessage.value = null
            try {
                authHelper.signInWithEmail(emailInput, passwordInput)
                _currentUser.value = authHelper.getCurrentUser()
                _authMessage.value = "Sign in successful!"
            } catch (e: Exception) {
                _authMessage.value = "Sign in error: ${e.message}"
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authMessage.value = null
            try {
                authHelper.signInWithGoogle()
                _currentUser.value = authHelper.getCurrentUser()
                _authMessage.value = "Signed in with Google"
            } catch (e: Exception) {
                _authMessage.value = "Google Auth Notice: Direct OAuth trigger requires browser redirect or Web client ID token."
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authHelper.signOut()
                _currentUser.value = null
                _authMessage.value = "Signed out."
            } catch (e: Exception) {
                _authMessage.value = "Sign out error: ${e.message}"
            }
        }
    }

    // --- SEED SAMPLE DATA IF DATABASE IS EMPTY ---
    private fun seedSampleDataIfDatabaseEmpty() {
        viewModelScope.launch {
            val existingSongs = mediaRepository.getAllSongs().first()
            if (existingSongs.isEmpty()) {
                // Seed Songs
                val sampleSongs = listOf(
                    SongEntity(
                        title = "Hotel California",
                        artist = "Eagles",
                        albumArtist = "Eagles",
                        albumTitle = "Hotel California",
                        genre = "Classic Rock",
                        year = 1976,
                        trackNumber = 1,
                        duration = 391000L,
                        uri = "https://example.com/sample1.mp3",
                        artworkUri = "https://picsum.photos/300/300?random=1"
                    ),
                    SongEntity(
                        title = "New Kid in Town",
                        artist = "Eagles",
                        albumArtist = "Eagles",
                        albumTitle = "Hotel California",
                        genre = "Classic Rock",
                        year = 1976,
                        trackNumber = 2,
                        duration = 304000L,
                        uri = "https://example.com/sample2.mp3",
                        artworkUri = "https://picsum.photos/300/300?random=1"
                    ),
                    SongEntity(
                        title = "Billie Jean",
                        artist = "Michael Jackson",
                        albumArtist = "Michael Jackson",
                        albumTitle = "Thriller",
                        genre = "Pop",
                        year = 1982,
                        trackNumber = 6,
                        duration = 294000L,
                        uri = "https://example.com/sample3.mp3",
                        artworkUri = "https://picsum.photos/300/300?random=2"
                    ),
                    SongEntity(
                        title = "Beat It",
                        artist = "Michael Jackson",
                        albumArtist = "Michael Jackson",
                        albumTitle = "Thriller",
                        genre = "Pop",
                        year = 1982,
                        trackNumber = 5,
                        duration = 258000L,
                        uri = "https://example.com/sample4.mp3",
                        artworkUri = "https://picsum.photos/300/300?random=2"
                    ),
                    SongEntity(
                        title = "Thriller (Official Music Video)",
                        artist = "Michael Jackson",
                        albumArtist = "Michael Jackson",
                        albumTitle = "Thriller",
                        genre = "Pop",
                        year = 1982,
                        trackNumber = 1,
                        duration = 822000L,
                        uri = "https://example.com/sample_video.mp4",
                        artworkUri = "https://picsum.photos/300/300?random=3",
                        isMusicVideo = true
                    )
                )
                mediaRepository.insertSongs(sampleSongs)

                // Seed Albums
                val sampleAlbums = listOf(
                    AlbumEntity(
                        title = "Hotel California",
                        albumArtist = "Eagles",
                        genre = "Classic Rock",
                        releaseYear = 1976,
                        albumCoverUri = "https://picsum.photos/300/300?random=1"
                    ),
                    AlbumEntity(
                        title = "Thriller",
                        albumArtist = "Michael Jackson",
                        genre = "Pop",
                        releaseYear = 1982,
                        albumCoverUri = "https://picsum.photos/300/300?random=2"
                    )
                )
                sampleAlbums.forEach { mediaRepository.insertAlbum(it) }

                // Seed Playlists
                val samplePlaylists = listOf(
                    PlaylistEntity(title = "80s Classics"),
                    PlaylistEntity(title = "Roadtrip Favorites")
                )
                samplePlaylists.forEach { mediaRepository.insertPlaylist(it) }

                // Seed TV Shows & Episodes
                val breakingBadId = mediaRepository.insertTvShow(
                    TvShowEntity(
                        title = "Breaking Bad",
                        releaseYear = 2008,
                        parentalRating = "TV-MA",
                        logoThumbnailUri = "https://picsum.photos/300/300?random=10"
                    )
                )
                val strangerThingsId = mediaRepository.insertTvShow(
                    TvShowEntity(
                        title = "Stranger Things",
                        releaseYear = 2016,
                        parentalRating = "TV-14",
                        logoThumbnailUri = "https://picsum.photos/300/300?random=11"
                    )
                )

                mediaRepository.insertTvEpisode(
                    TvEpisodeEntity(
                        title = "Pilot",
                        showTitle = "Breaking Bad",
                        seasonNumber = 1,
                        episodeNumber = 1,
                        uri = "https://example.com/bb_s1e1.mp4",
                        thumbnailUri = "https://picsum.photos/300/200?random=12"
                    )
                )
                mediaRepository.insertTvEpisode(
                    TvEpisodeEntity(
                        title = "Cat's in the Bag...",
                        showTitle = "Breaking Bad",
                        seasonNumber = 1,
                        episodeNumber = 2,
                        uri = "https://example.com/bb_s1e2.mp4",
                        thumbnailUri = "https://picsum.photos/300/200?random=13"
                    )
                )
                mediaRepository.insertTvEpisode(
                    TvEpisodeEntity(
                        title = "No-Rough-Stuff-Type Deal",
                        showTitle = "Breaking Bad",
                        seasonNumber = 2,
                        episodeNumber = 1,
                        uri = "https://example.com/bb_s2e1.mp4",
                        thumbnailUri = "https://picsum.photos/300/200?random=14"
                    )
                )
                mediaRepository.insertTvEpisode(
                    TvEpisodeEntity(
                        title = "Chapter One: The Vanishing of Will Byers",
                        showTitle = "Stranger Things",
                        seasonNumber = 1,
                        episodeNumber = 1,
                        uri = "https://example.com/st_s1e1.mp4",
                        thumbnailUri = "https://picsum.photos/300/200?random=15"
                    )
                )

                // Seed Movies
                mediaRepository.insertMovie(
                    MovieEntity(
                        title = "Inception",
                        artists = "Leonardo DiCaprio, Joseph Gordon-Levitt",
                        releaseYear = 2010,
                        parentalRating = "PG-13",
                        thumbnailUri = "https://picsum.photos/300/450?random=20",
                        uri = "https://example.com/inception.mp4"
                    )
                )
                mediaRepository.insertMovie(
                    MovieEntity(
                        title = "The Dark Knight",
                        artists = "Christian Bale, Heath Ledger",
                        releaseYear = 2008,
                        parentalRating = "PG-13",
                        thumbnailUri = "https://picsum.photos/300/450?random=21",
                        uri = "https://example.com/darkknight.mp4"
                    )
                )

                // Seed Podcast Shows & Episodes
                mediaRepository.insertPodcastShow(
                    PodcastShowEntity(
                        title = "The Daily",
                        creatorHost = "The New York Times",
                        description = "This is how the news should sound. Twenty minutes a day, five days a week.",
                        category = "News",
                        logoThumbnailUri = "https://picsum.photos/300/300?random=30"
                    )
                )
                mediaRepository.insertPodcastShow(
                    PodcastShowEntity(
                        title = "Lex Fridman Podcast",
                        creatorHost = "Lex Fridman",
                        description = "Conversations about AI, science, technology, history, philosophy, and the nature of intelligence.",
                        category = "Technology",
                        logoThumbnailUri = "https://picsum.photos/300/300?random=31"
                    )
                )

                mediaRepository.insertPodcastEpisode(
                    PodcastEpisodeEntity(
                        title = "#350 – Sam Altman: OpenAI, GPT-4, and AI Safety",
                        podcastName = "Lex Fridman Podcast",
                        seasonNumber = 1,
                        episodeNumber = 350,
                        uri = "https://example.com/lex_350.mp3"
                    )
                )
                mediaRepository.insertPodcastEpisode(
                    PodcastEpisodeEntity(
                        title = "The Global AI Race Explained",
                        podcastName = "The Daily",
                        seasonNumber = 2024,
                        episodeNumber = 12,
                        uri = "https://example.com/daily_ai.mp3"
                    )
                )

                // Seed Audiobooks & Book Series
                mediaRepository.insertBookSeries(
                    BookSeriesEntity(
                        title = "Harry Potter Series",
                        genre = "Fantasy",
                        portraitCoverUri = "https://picsum.photos/300/450?random=40"
                    )
                )
                mediaRepository.insertAudiobook(
                    AudiobookEntity(
                        title = "Harry Potter and the Sorcerer's Stone",
                        author = "J.K. Rowling",
                        bookSeries = "Harry Potter Series",
                        bookNumberInSeries = 1,
                        genre = "Fantasy",
                        publicationYear = 1997,
                        portraitCoverUri = "https://picsum.photos/300/450?random=41",
                        uri = "https://example.com/hp1.mp3"
                    )
                )
                mediaRepository.insertAudiobook(
                    AudiobookEntity(
                        title = "Dune",
                        author = "Frank Herbert",
                        bookSeries = "Dune Chronicles",
                        bookNumberInSeries = 1,
                        genre = "Sci-Fi",
                        publicationYear = 1965,
                        portraitCoverUri = "https://picsum.photos/300/450?random=42",
                        uri = "https://example.com/dune.mp3"
                    )
                )
            }
        }
    }

    fun importFiles(uris: List<Uri>, category: ImportCategory) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            for (uri in uris) {
                when (category) {
                    ImportCategory.MUSIC -> {
                        val song = MediaImportHelper.createSongEntity(context, uri)
                        mediaRepository.insertSong(song)
                    }
                    ImportCategory.TV_SHOWS -> {
                        val episode = MediaImportHelper.createTvEpisodeEntity(context, uri)
                        mediaRepository.insertTvEpisode(episode)
                    }
                    ImportCategory.MOVIES -> {
                        val movie = MediaImportHelper.createMovieEntity(context, uri)
                        mediaRepository.insertMovie(movie)
                    }
                    ImportCategory.PODCASTS -> {
                        val episode = MediaImportHelper.createPodcastEpisodeEntity(context, uri)
                        mediaRepository.insertPodcastEpisode(episode)
                    }
                    ImportCategory.AUDIOBOOKS -> {
                        val audiobook = MediaImportHelper.createAudiobookEntity(context, uri)
                        mediaRepository.insertAudiobook(audiobook)
                    }
                    ImportCategory.GAMES -> {
                        // Games handled via installed applications list
                    }
                }
            }
        }
    }
}

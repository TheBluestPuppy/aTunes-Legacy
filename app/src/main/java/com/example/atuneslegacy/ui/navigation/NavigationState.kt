package com.example.atuneslegacy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    MUSIC("Music", Icons.Default.MusicNote),
    TV_SHOWS("TV Shows", Icons.Default.Tv),
    MOVIES("Movies", Icons.Default.Movie),
    PODCASTS("Podcasts", Icons.Default.Podcasts),
    AUDIOBOOKS("Audiobooks", Icons.Default.Book),
    PHOTOS("Photos", Icons.Default.Image),
    RECORDINGS("Recordings", Icons.Default.Mic),
    GAMES("Games", Icons.Default.SportsEsports),
    SETTINGS("Settings", Icons.Default.Settings)
}

enum class MusicSubTab(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres"),
    PLAYLISTS("Playlists"),
    MUSIC_VIDEOS("Music Videos")
}

enum class PodcastSubTab(val title: String) {
    SHOWS("Podcasts"),
    CATEGORIES("Categories")
}

enum class AudiobookSubTab(val title: String) {
    BOOKS("Books"),
    AUTHORS("Authors"),
    SERIES("Series"),
    GENRES("Genres")
}

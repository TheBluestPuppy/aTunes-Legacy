package com.example.atuneslegacy.ui.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import com.example.atuneslegacy.ui.components.AlbumGridCard
import com.example.atuneslegacy.ui.components.AudiobookCard
import com.example.atuneslegacy.ui.components.ITunesSegmentedControl
import com.example.atuneslegacy.ui.components.ITunesTopBar
import com.example.atuneslegacy.ui.components.MovieCard
import com.example.atuneslegacy.ui.components.PodcastShowCard
import com.example.atuneslegacy.ui.components.SongTableHeader
import com.example.atuneslegacy.ui.components.SongTableRow
import com.example.atuneslegacy.ui.components.TvShowCard
import com.example.atuneslegacy.ui.navigation.MainTab
import com.example.atuneslegacy.ui.navigation.MusicSubTab
import com.example.atuneslegacy.ui.theme.ATunesLegacyTheme

@Preview(name = "iTunes 11 Top Bar", showBackground = true)
@Composable
fun PreviewTopBar() {
    ATunesLegacyTheme {
        ITunesTopBar(
            currentTab = MainTab.MUSIC,
            onTabSelected = {},
            searchQuery = "",
            onSearchQueryChanged = {}
        )
    }
}

@Preview(name = "Sub-Tab Segmented Control", showBackground = true)
@Composable
fun PreviewSegmentedControl() {
    ATunesLegacyTheme {
        ITunesSegmentedControl(
            items = MusicSubTab.entries,
            selectedItem = MusicSubTab.SONGS,
            onItemSelected = {},
            getItemLabel = { it.title }
        )
    }
}

@Preview(name = "Song Table Row", showBackground = true)
@Composable
fun PreviewSongTableRow() {
    ATunesLegacyTheme {
        Surface {
            SongTableRow(
                song = SongEntity(
                    title = "Hotel California",
                    artist = "Eagles",
                    albumArtist = "Eagles",
                    albumTitle = "Hotel California",
                    genre = "Classic Rock",
                    year = 1976,
                    trackNumber = 1,
                    duration = 391000L,
                    uri = ""
                ),
                index = 0,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Album Grid Card", showBackground = true)
@Composable
fun PreviewAlbumGridCard() {
    ATunesLegacyTheme {
        Surface {
            AlbumGridCard(
                album = AlbumEntity(
                    title = "Hotel California",
                    albumArtist = "Eagles",
                    genre = "Classic Rock",
                    releaseYear = 1976
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "TV Show Card", showBackground = true)
@Composable
fun PreviewTvShowCard() {
    ATunesLegacyTheme {
        Surface {
            TvShowCard(
                show = TvShowEntity(
                    title = "Breaking Bad",
                    releaseYear = 2008,
                    parentalRating = "TV-MA"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "Movie Card", showBackground = true)
@Composable
fun PreviewMovieCard() {
    ATunesLegacyTheme {
        Surface {
            MovieCard(
                movie = MovieEntity(
                    title = "Inception",
                    artists = "Leonardo DiCaprio",
                    releaseYear = 2010,
                    parentalRating = "PG-13",
                    uri = ""
                ),
                isGrid = true,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Podcast Show Card", showBackground = true)
@Composable
fun PreviewPodcastShowCard() {
    ATunesLegacyTheme {
        Surface {
            PodcastShowCard(
                show = PodcastShowEntity(
                    title = "The Daily",
                    creatorHost = "The New York Times",
                    category = "News"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "Audiobook Card", showBackground = true)
@Composable
fun PreviewAudiobookCard() {
    ATunesLegacyTheme {
        Surface {
            AudiobookCard(
                book = AudiobookEntity(
                    title = "Harry Potter and the Sorcerer's Stone",
                    author = "J.K. Rowling",
                    genre = "Fantasy",
                    uri = ""
                ),
                onClick = {}
            )
        }
    }
}

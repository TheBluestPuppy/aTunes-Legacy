package com.example.atuneslegacy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.R
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import com.example.atuneslegacy.ui.theme.ATunesLegacyTheme
import com.example.atuneslegacy.ui.theme.ITunesAccentBlue

// Helper formatting function for song duration (e.g., 234000ms -> "3:54")
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "--:--"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

// 1. SONG TABLE HEADER
@Composable
fun SongTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E6ED))
            .border(width = 0.5.dp, color = Color(0xFFC0C5CE))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
        Text(text = "Title", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
        Text(text = "Time", modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
        Text(text = "Artist", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
        Text(text = "Album", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
        Text(text = "Genre", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4A4E58))
    }
}

// 2. SONG TABLE ROW
@Composable
fun SongTableRow(
    song: SongEntity,
    index: Int,
    isSelected: Boolean = false,
    showArtwork: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF388BFD).copy(alpha = 0.2f)
        index % 2 == 1 -> Color(0xFFF7F8FA)
        else -> Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = song.trackNumber.toString(),
            modifier = Modifier.width(30.dp),
            fontSize = 12.sp,
            color = Color(0xFF707682)
        )
        Row(
            modifier = Modifier.weight(2.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showArtwork) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E6EE))
                        .border(0.5.dp, Color(0xFFC0C5CE), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.artworkUri.isNotBlank()) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = ITunesAccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else if (song.isMusicVideo) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Music Video",
                    tint = ITunesAccentBlue,
                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = song.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1D21),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatDuration(song.duration),
            modifier = Modifier.width(55.dp),
            fontSize = 12.sp,
            color = Color(0xFF5A606C)
        )
        Text(
            text = song.artist,
            modifier = Modifier.weight(2f),
            fontSize = 12.sp,
            color = Color(0xFF404550),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.albumTitle,
            modifier = Modifier.weight(2f),
            fontSize = 12.sp,
            color = Color(0xFF404550),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.genre,
            modifier = Modifier.weight(1.5f),
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
}

// 3. ALBUM GRID CARD
@Composable
fun AlbumGridCard(
    album: AlbumEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(138.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E3EA))
                .border(1.dp, Color(0xFFC8CCD6), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (album.albumCoverUri.isNotBlank()) {
                AsyncImage(
                    model = album.albumCoverUri,
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF9098A8),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1D21),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.albumArtist} • ${album.releaseYear}",
            fontSize = 11.sp,
            color = Color(0xFF636A78),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 4. TV SHOW CARD
@Composable
fun TvShowCard(
    show: TvShowEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E6EE))
                    .border(1.dp, Color(0xFFC4C9D4), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (show.logoThumbnailUri.isNotBlank()) {
                    AsyncImage(
                        model = show.logoThumbnailUri,
                        contentDescription = show.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF8890A0),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = show.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1D21)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Release: ${show.releaseYear}",
                        fontSize = 12.sp,
                        color = Color(0xFF636A78)
                    )
                    if (show.parentalRating.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .border(0.8.dp, Color(0xFF8E95A2), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = show.parentalRating,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A505C)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. TV EPISODE ROW
@Composable
fun TvEpisodeRow(
    episode: TvEpisodeEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E222A)),
            contentAlignment = Alignment.Center
        ) {
            if (episode.thumbnailUri.isNotBlank()) {
                AsyncImage(
                    model = episode.thumbnailUri,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.6f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Episode ${episode.episodeNumber}: ${episode.title}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1D21)
            )
            Text(
                text = "Season ${episode.seasonNumber}",
                fontSize = 11.sp,
                color = Color(0xFF687080)
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
}

// 6. MOVIE CARD
@Composable
fun MovieCard(
    movie: MovieEntity,
    isGrid: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isGrid) {
        Column(
            modifier = modifier
                .width(120.dp)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF181B20))
                    .border(1.dp, Color(0xFFC8CCD6), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (movie.thumbnailUri.isNotBlank()) {
                    AsyncImage(
                        model = movie.thumbnailUri,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(0.7f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1D21),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${movie.releaseYear} • ${movie.parentalRating}",
                fontSize = 10.sp,
                color = Color(0xFF636A78),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF181B20)),
                contentAlignment = Alignment.Center
            ) {
                if (movie.thumbnailUri.isNotBlank()) {
                    AsyncImage(
                        model = movie.thumbnailUri,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = movie.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1D21))
                Text(text = "Cast: ${movie.artists}", fontSize = 12.sp, color = Color(0xFF525866))
                Text(text = "Year: ${movie.releaseYear} | Rating: ${movie.parentalRating}", fontSize = 11.sp, color = Color(0xFF788090))
            }
        }
        HorizontalDivider(color = Color(0xFFE8EBF0), thickness = 0.5.dp)
    }
}

// 7. PODCAST SHOW CARD
@Composable
fun PodcastShowCard(
    show: PodcastShowEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(130.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE2E6EE))
                .border(1.dp, Color(0xFFC4C9D4), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (show.logoThumbnailUri.isNotBlank()) {
                AsyncImage(
                    model = show.logoThumbnailUri,
                    contentDescription = show.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Podcasts,
                    contentDescription = null,
                    tint = ITunesAccentBlue,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = show.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1D21),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = show.creatorHost,
            fontSize = 11.sp,
            color = Color(0xFF636A78),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 8. AUDIOBOOK CARD (Portrait Orientation)
@Composable
fun AudiobookCard(
    book: AudiobookEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(175.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE4E8F0))
                .border(1.dp, Color(0xFFC0C6D2), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (book.portraitCoverUri.isNotBlank()) {
                AsyncImage(
                    model = book.portraitCoverUri,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.68f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = Color(0xFF606878),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = book.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1D21),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = book.author,
            fontSize = 11.sp,
            color = Color(0xFF636A78),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

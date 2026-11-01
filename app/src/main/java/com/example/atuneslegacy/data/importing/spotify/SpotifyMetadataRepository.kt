package com.example.atuneslegacy.data.importing.spotify

import com.example.atuneslegacy.data.remote.KtorHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyOEmbedDto(
    val title: String? = null,
    val author_name: String? = null,
    val thumbnail_url: String? = null
)

data class UnifiedTrackMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val artists: List<String>,
    val albumName: String,
    val albumId: String,
    val artworkUrl: String,
    val durationMs: Long,
    val releaseDate: String,
    val trackNumber: Int,
    val discNumber: Int,
    val explicit: Boolean,
    val spotifyUrl: String
)

object SpotifyMetadataRepository {
    private val client: HttpClient = KtorHttpClient.client

    suspend fun fetchMetadata(link: ParsedSpotifyLink): List<UnifiedTrackMetadata> {
        if (link.type == SpotifyContentType.RAW_QUERY) {
            val query = link.originalUrl
            return listOf(
                UnifiedTrackMetadata(
                    id = "query_${query.hashCode()}",
                    title = query,
                    artist = "Various Artists",
                    artists = listOf("Various Artists"),
                    albumName = "$query - Single",
                    albumId = "album_query_${query.hashCode()}",
                    artworkUrl = "",
                    durationMs = 210000L,
                    releaseDate = "2024",
                    trackNumber = 1,
                    discNumber = 1,
                    explicit = false,
                    spotifyUrl = ""
                )
            )
        }

        return try {
            val oembed: SpotifyOEmbedDto = client.get("https://open.spotify.com/oembed") {
                parameter("url", link.originalUrl)
            }.body()

            val title = oembed.title ?: "Spotify Track"
            val artist = oembed.author_name ?: "Spotify Artist"
            val artwork = oembed.thumbnail_url ?: ""

            listOf(
                UnifiedTrackMetadata(
                    id = link.id,
                    title = title,
                    artist = artist,
                    artists = listOf(artist),
                    albumName = "$title - Single",
                    albumId = "album_${link.id}",
                    artworkUrl = artwork,
                    durationMs = 210000L,
                    releaseDate = "2024",
                    trackNumber = 1,
                    discNumber = 1,
                    explicit = false,
                    spotifyUrl = link.originalUrl
                )
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}

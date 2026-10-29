package com.example.atuneslegacy.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzSearchResponse(
    val recordings: List<MusicBrainzRecording> = emptyList()
)

@Serializable
data class MusicBrainzRecording(
    val id: String? = null,
    val title: String? = null,
    val length: Long? = null,
    val artist_credit: List<MusicBrainzArtistCredit> = emptyList(),
    val releases: List<MusicBrainzRelease> = emptyList()
)

@Serializable
data class MusicBrainzArtistCredit(
    val name: String? = null,
    val artist: MusicBrainzArtist? = null
)

@Serializable
data class MusicBrainzArtist(
    val name: String? = null
)

@Serializable
data class MusicBrainzRelease(
    val id: String? = null,
    val title: String? = null,
    val date: String? = null
)

@Serializable
data class AcoustIdResponse(
    val status: String? = null,
    val results: List<AcoustIdResult> = emptyList()
)

@Serializable
data class AcoustIdResult(
    val id: String? = null,
    val score: Double? = null,
    val recordings: List<AcoustIdRecording> = emptyList()
)

@Serializable
data class AcoustIdRecording(
    val id: String? = null,
    val title: String? = null,
    val artists: List<AcoustIdArtist> = emptyList()
)

@Serializable
data class AcoustIdArtist(
    val name: String? = null
)

data class TrackMetadataResult(
    val title: String,
    val artist: String,
    val albumTitle: String,
    val year: Int,
    val artworkUrl: String?
)

object MusicBrainzApi {
    private val client: HttpClient = KtorHttpClient.client
    private const val ACOUSTID_CLIENT_KEY = "8cp2Z7x9"

    suspend fun searchRecording(query: String): TrackMetadataResult? {
        if (query.isBlank()) return null
        return try {
            val response: MusicBrainzSearchResponse = client.get("https://musicbrainz.org/ws/2/recording/") {
                parameter("query", query)
                parameter("fmt", "json")
            }.body()

            val rec = response.recordings.firstOrNull() ?: return null
            val title = rec.title ?: query
            val artist = rec.artist_credit.firstOrNull()?.let { it.artist?.name ?: it.name } ?: "Unknown Artist"
            val release = rec.releases.firstOrNull()
            val albumTitle = release?.title ?: "$title - Album"
            val yearStr = release?.date?.take(4)
            val year = yearStr?.toIntOrNull() ?: 2024
            val releaseId = release?.id
            val coverUrl = if (!releaseId.isNullOrBlank()) "https://coverartarchive.org/release/$releaseId/front" else null

            TrackMetadataResult(
                title = title,
                artist = artist,
                albumTitle = albumTitle,
                year = year,
                artworkUrl = coverUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun identifyAudioFileAcoustId(durationSeconds: Int, fingerprint: String = "sample_fingerprint"): TrackMetadataResult? {
        return try {
            val response: AcoustIdResponse = client.get("https://api.acoustid.org/v2/lookup") {
                parameter("client", ACOUSTID_CLIENT_KEY)
                parameter("duration", durationSeconds)
                parameter("fingerprint", fingerprint)
                parameter("meta", "recordings+releases")
            }.body()

            val bestResult = response.results.maxByOrNull { it.score ?: 0.0 }
            val rec = bestResult?.recordings?.firstOrNull()
            if (rec != null) {
                val title = rec.title ?: "Identified Track"
                val artist = rec.artists.firstOrNull()?.name ?: "AcoustID Artist"
                TrackMetadataResult(
                    title = title,
                    artist = artist,
                    albumTitle = "$title - EP",
                    year = 2024,
                    artworkUrl = "https://picsum.photos/300/300?random=100"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

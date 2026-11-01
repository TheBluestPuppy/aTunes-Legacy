package com.example.atuneslegacy.data.importing.spotify

import com.example.atuneslegacy.data.remote.KtorHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveSearchDto(
    val response: ArchiveSearchHeaderDto? = null
)

@Serializable
data class ArchiveSearchHeaderDto(
    val docs: List<ArchiveSearchDocDto> = emptyList()
)

@Serializable
data class ArchiveSearchDocDto(
    val identifier: String? = null,
    val title: String? = null,
    val creator: String? = null,
    val downloads: Long? = null
)

@Serializable
data class ArchiveFilesDto(
    val files: List<ArchiveFileDto> = emptyList()
)

@Serializable
data class ArchiveFileDto(
    val name: String? = null,
    val format: String? = null,
    val size: Long = 0L
)

data class CandidateRecording(
    val title: String,
    val artist: String,
    val downloadUrl: String,
    val fileSize: Long,
    val format: String,
    val providerName: String
)

object MusicSearchEngine {
    private val client: HttpClient = KtorHttpClient.client

    suspend fun search(metadata: UnifiedTrackMetadata): List<CandidateRecording> {
        val candidates = mutableListOf<CandidateRecording>()
        try {
            val query = "title:(\"${metadata.title}\") AND creator:(\"${metadata.artist}\") AND mediatype:(audio)"
            val res: ArchiveSearchDto = client.get("https://archive.org/advancedsearch.php") {
                parameter("q", query)
                parameter("rows", "5")
                parameter("output", "json")
            }.body()

            val docs = res.response?.docs ?: emptyList()
            for (doc in docs) {
                if (doc.identifier.isNullOrBlank()) continue
                try {
                    val filesRes: ArchiveFilesDto = client.get("https://archive.org/metadata/${doc.identifier}").body()
                    val mp3File = filesRes.files.firstOrNull { it.format.equals("VBR MP3", true) || it.format.equals("MP3", true) || it.name?.endsWith(".mp3", true) == true }
                    if (mp3File != null && !mp3File.name.isNullOrBlank()) {
                        val url = "https://archive.org/download/${doc.identifier}/${mp3File.name}"
                        candidates.add(
                            CandidateRecording(
                                title = doc.title ?: metadata.title,
                                artist = doc.creator ?: metadata.artist,
                                downloadUrl = url,
                                fileSize = mp3File.size,
                                format = "MP3",
                                providerName = "Internet Archive"
                            )
                        )
                    }
                } catch (e: Exception) { }
            }
        } catch (e: Exception) { }

        // NO MOCK PLACEHOLDER AUDIO FALLBACK. Returns actual archive results or empty list.
        return candidates
    }
}

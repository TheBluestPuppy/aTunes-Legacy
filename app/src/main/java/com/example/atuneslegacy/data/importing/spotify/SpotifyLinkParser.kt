package com.example.atuneslegacy.data.importing.spotify

enum class SpotifyContentType {
    TRACK, ALBUM, PLAYLIST, ARTIST, RAW_QUERY, UNKNOWN
}

data class ParsedSpotifyLink(
    val type: SpotifyContentType,
    val id: String,
    val originalUrl: String
)

object SpotifyLinkParser {
    fun parse(input: String): ParsedSpotifyLink {
        val cleaned = input.trim()
        if (cleaned.isBlank()) {
            return ParsedSpotifyLink(SpotifyContentType.UNKNOWN, "", cleaned)
        }

        if (cleaned.startsWith("spotify:")) {
            val parts = cleaned.split(":")
            if (parts.size >= 3) {
                val typeStr = parts[1].lowercase()
                val id = parts[2].split("?").first()
                val type = when (typeStr) {
                    "track" -> SpotifyContentType.TRACK
                    "album" -> SpotifyContentType.ALBUM
                    "playlist" -> SpotifyContentType.PLAYLIST
                    "artist" -> SpotifyContentType.ARTIST
                    else -> SpotifyContentType.UNKNOWN
                }
                if (type != SpotifyContentType.UNKNOWN && id.isNotBlank()) {
                    return ParsedSpotifyLink(type, id, cleaned)
                }
            }
        }

        if (cleaned.contains("spotify.com/", ignoreCase = true)) {
            val pathSegments = cleaned.split("?").first().split("/")
            val typeIdx = pathSegments.indexOfFirst { it in listOf("track", "album", "playlist", "artist") }
            if (typeIdx != -1 && typeIdx + 1 < pathSegments.size) {
                val typeStr = pathSegments[typeIdx].lowercase()
                val id = pathSegments[typeIdx + 1]
                val type = when (typeStr) {
                    "track" -> SpotifyContentType.TRACK
                    "album" -> SpotifyContentType.ALBUM
                    "playlist" -> SpotifyContentType.PLAYLIST
                    "artist" -> SpotifyContentType.ARTIST
                    else -> SpotifyContentType.UNKNOWN
                }
                if (type != SpotifyContentType.UNKNOWN && id.isNotBlank()) {
                    return ParsedSpotifyLink(type, id, cleaned)
                }
            }
        }

        return ParsedSpotifyLink(SpotifyContentType.RAW_QUERY, cleaned, cleaned)
    }
}

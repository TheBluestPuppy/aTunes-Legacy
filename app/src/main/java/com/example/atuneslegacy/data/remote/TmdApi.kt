package com.example.atuneslegacy.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class TmdMovieResponse(
    val results: List<TmdMovieResult> = emptyList()
)

@Serializable
data class TmdMovieResult(
    val title: String? = null,
    val release_date: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null
)

@Serializable
data class TmdTvResponse(
    val results: List<TmdTvResult> = emptyList()
)

@Serializable
data class TmdTvResult(
    val name: String? = null,
    val first_air_date: String? = null,
    val overview: String? = null,
    val vote_average: Double? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null
)

object TmdApi {
    private val client: HttpClient = KtorHttpClient.client
    var apiKey: String = "3b38148b1397b76e271bf9e2c24efb48"

    suspend fun searchMovie(query: String): TmdMovieResult? {
        if (query.isBlank()) return null
        return try {
            val response: TmdMovieResponse = client.get("https://api.themoviedb.org/3/search/movie") {
                parameter("api_key", apiKey)
                parameter("query", query)
            }.body()
            response.results.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchTvShow(query: String): TmdTvResult? {
        if (query.isBlank()) return null
        return try {
            val response: TmdTvResponse = client.get("https://api.themoviedb.org/3/search/tv") {
                parameter("api_key", apiKey)
                parameter("query", query)
            }.body()
            response.results.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun getImageUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        return if (path.startsWith("http")) path else "https://image.tmdb.org/t/p/w500$path"
    }
}

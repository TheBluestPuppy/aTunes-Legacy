package com.example.atuneslegacy.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorHttpClient {
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            defaultRequest {
                // MusicBrainz requires a descriptive User-Agent header
                header(HttpHeaders.UserAgent, "aTunesLegacy/1.0.0 (https://github.com/example/atuneslegacy)")
                header(HttpHeaders.Accept, "application/json")
            }
        }
    }
}

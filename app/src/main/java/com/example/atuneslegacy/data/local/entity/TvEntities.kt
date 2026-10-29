package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tv_shows")
data class TvShowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val releaseYear: Int,
    val parentalRating: String = "",
    val logoThumbnailUri: String = ""
)

@Entity(tableName = "tv_episodes")
data class TvEpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val showTitle: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val videoPath: String = "",
    val uri: String,
    val thumbnailUri: String = ""
)

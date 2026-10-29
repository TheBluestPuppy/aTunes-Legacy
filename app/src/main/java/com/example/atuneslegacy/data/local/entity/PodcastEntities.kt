package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcast_shows")
data class PodcastShowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val creatorHost: String = "",
    val description: String = "",
    val category: String = "",
    val logoThumbnailUri: String = ""
)

@Entity(tableName = "podcast_episodes")
data class PodcastEpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val podcastName: String,
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1,
    val audioPath: String = "",
    val uri: String
)

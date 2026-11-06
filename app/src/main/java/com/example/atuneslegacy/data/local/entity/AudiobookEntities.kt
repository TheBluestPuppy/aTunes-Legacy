package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookChapter(
    val time: String,
    val timeMs: Long,
    val title: String,
    val number: Int
)

@Entity(tableName = "audiobooks")
data class AudiobookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val bookSeries: String = "",
    val bookNumberInSeries: Int = 1,
    val genre: String = "",
    val publicationYear: Int = 0,
    val portraitCoverUri: String = "",
    val audioPath: String = "",
    val uri: String,
    val lastPlaybackPositionMs: Long = 0L,
    val description: String = "",
    val chaptersJson: String = ""
)

@Entity(tableName = "book_series")
data class BookSeriesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val genre: String = "",
    val portraitCoverUri: String = ""
)

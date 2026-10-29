package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artists: String,
    val releaseYear: Int,
    val parentalRating: String = "",
    val thumbnailUri: String = "",
    val videoPath: String = "",
    val uri: String
)

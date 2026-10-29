package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val albumArtist: String,
    val genre: String,
    val releaseYear: Int,
    val albumCoverUri: String = ""
)

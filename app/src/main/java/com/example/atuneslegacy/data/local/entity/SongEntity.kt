package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val albumTitle: String,
    val composer: String = "",
    val genre: String,
    val year: Int,
    val trackNumber: Int = 1,
    val filePath: String = "",
    val uri: String,
    val duration: Long = 0L,
    val artworkUri: String = "",
    val isMusicVideo: Boolean = false
)

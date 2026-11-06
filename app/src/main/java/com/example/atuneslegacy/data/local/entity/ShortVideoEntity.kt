package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "short_videos")
data class ShortVideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val uri: String,
    val isSaved: Boolean = false,
    val savedTimestamp: Long = 0L
)

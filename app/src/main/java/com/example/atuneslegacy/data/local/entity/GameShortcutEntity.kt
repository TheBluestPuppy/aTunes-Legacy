package com.example.atuneslegacy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_shortcuts")
data class GameShortcutEntity(
    @PrimaryKey val packageName: String,
    val customName: String = "",
    val customImageUri: String = ""
)

package com.example.atuneslegacy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atuneslegacy.data.local.entity.ShortVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortsDao {
    @Query("SELECT * FROM short_videos ORDER BY id ASC")
    fun getAllShorts(): Flow<List<ShortVideoEntity>>

    @Query("SELECT * FROM short_videos WHERE isSaved = 1 ORDER BY savedTimestamp ASC")
    fun getSavedShorts(): Flow<List<ShortVideoEntity>>

    @Query("SELECT * FROM short_videos WHERE uri = :uri OR filePath = :uri LIMIT 1")
    suspend fun getShortByUri(uri: String): ShortVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShort(short: ShortVideoEntity): Long

    @Update
    suspend fun updateShort(short: ShortVideoEntity)

    @Delete
    suspend fun deleteShort(short: ShortVideoEntity)

    @Query("DELETE FROM short_videos")
    suspend fun deleteAllShorts()
}

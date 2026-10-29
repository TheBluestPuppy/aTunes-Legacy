package com.example.atuneslegacy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TvDao {
    // TV Show queries
    @Query("SELECT * FROM tv_shows ORDER BY title ASC")
    fun getAllTvShows(): Flow<List<TvShowEntity>>

    @Query("SELECT * FROM tv_shows WHERE id = :id")
    suspend fun getTvShowById(id: Long): TvShowEntity?

    @Query("SELECT * FROM tv_shows WHERE title = :title LIMIT 1")
    suspend fun getTvShowByTitle(title: String): TvShowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvShow(show: TvShowEntity): Long

    @Update
    suspend fun updateTvShow(show: TvShowEntity)

    @Delete
    suspend fun deleteTvShow(show: TvShowEntity)

    // TV Episode queries
    @Query("SELECT * FROM tv_episodes WHERE showTitle = :showTitle ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesByShow(showTitle: String): Flow<List<TvEpisodeEntity>>

    @Query("SELECT * FROM tv_episodes ORDER BY title ASC")
    fun getAllEpisodes(): Flow<List<TvEpisodeEntity>>

    @Query("SELECT * FROM tv_episodes WHERE id = :id")
    suspend fun getEpisodeById(id: Long): TvEpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: TvEpisodeEntity): Long

    @Update
    suspend fun updateEpisode(episode: TvEpisodeEntity)

    @Delete
    suspend fun deleteEpisode(episode: TvEpisodeEntity)

    // Cascade update for episodes when TV show title changes
    @Query("""
        UPDATE tv_episodes 
        SET showTitle = :newShowTitle 
        WHERE showTitle = :oldShowTitle
    """)
    suspend fun updateTvEpisodesShowTitle(oldShowTitle: String, newShowTitle: String)
}

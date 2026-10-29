package com.example.atuneslegacy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    // Podcast Shows
    @Query("SELECT * FROM podcast_shows ORDER BY title ASC")
    fun getAllPodcastShows(): Flow<List<PodcastShowEntity>>

    @Query("SELECT * FROM podcast_shows WHERE id = :id")
    suspend fun getPodcastShowById(id: Long): PodcastShowEntity?

    @Query("SELECT * FROM podcast_shows WHERE title = :title LIMIT 1")
    suspend fun getPodcastShowByTitle(title: String): PodcastShowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcastShow(show: PodcastShowEntity): Long

    @Update
    suspend fun updatePodcastShow(show: PodcastShowEntity)

    @Delete
    suspend fun deletePodcastShow(show: PodcastShowEntity)

    // Podcast Episodes
    @Query("SELECT * FROM podcast_episodes WHERE podcastName = :podcastName ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesByPodcast(podcastName: String): Flow<List<PodcastEpisodeEntity>>

    @Query("SELECT * FROM podcast_episodes ORDER BY title ASC")
    fun getAllPodcastEpisodes(): Flow<List<PodcastEpisodeEntity>>

    @Query("SELECT * FROM podcast_episodes WHERE id = :id")
    suspend fun getPodcastEpisodeById(id: Long): PodcastEpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcastEpisode(episode: PodcastEpisodeEntity): Long

    @Update
    suspend fun updatePodcastEpisode(episode: PodcastEpisodeEntity)

    @Delete
    suspend fun deletePodcastEpisode(episode: PodcastEpisodeEntity)

    // Cascade update for podcast episodes when podcast name changes
    @Query("""
        UPDATE podcast_episodes 
        SET podcastName = :newPodcastName 
        WHERE podcastName = :oldPodcastName
    """)
    suspend fun updatePodcastEpisodesPodcastName(oldPodcastName: String, newPodcastName: String)
}

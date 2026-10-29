package com.example.atuneslegacy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudiobookDao {
    // Audiobooks
    @Query("SELECT * FROM audiobooks ORDER BY title ASC")
    fun getAllAudiobooks(): Flow<List<AudiobookEntity>>

    @Query("SELECT * FROM audiobooks WHERE id = :id")
    suspend fun getAudiobookById(id: Long): AudiobookEntity?

    @Query("SELECT * FROM audiobooks WHERE bookSeries = :seriesTitle ORDER BY bookNumberInSeries ASC")
    fun getAudiobooksBySeries(seriesTitle: String): Flow<List<AudiobookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudiobook(audiobook: AudiobookEntity): Long

    @Update
    suspend fun updateAudiobook(audiobook: AudiobookEntity)

    @Delete
    suspend fun deleteAudiobook(audiobook: AudiobookEntity)

    // Book Series
    @Query("SELECT * FROM book_series ORDER BY title ASC")
    fun getAllBookSeries(): Flow<List<BookSeriesEntity>>

    @Query("SELECT * FROM book_series WHERE id = :id")
    suspend fun getBookSeriesById(id: Long): BookSeriesEntity?

    @Query("SELECT * FROM book_series WHERE title = :title LIMIT 1")
    suspend fun getBookSeriesByTitle(title: String): BookSeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookSeries(series: BookSeriesEntity): Long

    @Update
    suspend fun updateBookSeries(series: BookSeriesEntity)

    @Delete
    suspend fun deleteBookSeries(series: BookSeriesEntity)

    // Cascade update for audiobooks when book series metadata changes
    @Query("""
        UPDATE audiobooks 
        SET bookSeries = :newSeriesTitle, genre = :newGenre 
        WHERE bookSeries = :oldSeriesTitle
    """)
    suspend fun updateAudiobooksSeriesMetadata(
        oldSeriesTitle: String,
        newSeriesTitle: String,
        newGenre: String
    )
}

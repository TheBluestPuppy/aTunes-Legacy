package com.example.atuneslegacy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.atuneslegacy.data.local.dao.AudiobookDao
import com.example.atuneslegacy.data.local.dao.MovieDao
import com.example.atuneslegacy.data.local.dao.MusicDao
import com.example.atuneslegacy.data.local.dao.PodcastDao
import com.example.atuneslegacy.data.local.dao.TvDao
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.AudiobookEntity
import com.example.atuneslegacy.data.local.entity.BookSeriesEntity
import com.example.atuneslegacy.data.local.entity.MovieEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.PlaylistItemEntity
import com.example.atuneslegacy.data.local.entity.PodcastEpisodeEntity
import com.example.atuneslegacy.data.local.entity.PodcastShowEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import com.example.atuneslegacy.data.local.entity.TvEpisodeEntity
import com.example.atuneslegacy.data.local.entity.TvShowEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        TvShowEntity::class,
        TvEpisodeEntity::class,
        MovieEntity::class,
        PodcastShowEntity::class,
        PodcastEpisodeEntity::class,
        AudiobookEntity::class,
        BookSeriesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun tvDao(): TvDao
    abstract fun movieDao(): MovieDao
    abstract fun podcastDao(): PodcastDao
    abstract fun audiobookDao(): AudiobookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atunes_legacy.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

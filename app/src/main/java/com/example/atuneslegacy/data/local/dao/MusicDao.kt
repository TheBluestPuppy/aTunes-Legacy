package com.example.atuneslegacy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atuneslegacy.data.local.entity.AlbumEntity
import com.example.atuneslegacy.data.local.entity.PlaylistEntity
import com.example.atuneslegacy.data.local.entity.PlaylistItemEntity
import com.example.atuneslegacy.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Song queries
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isMusicVideo = 0 ORDER BY title ASC")
    fun getMusicSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isMusicVideo = 1 ORDER BY title ASC")
    fun getMusicVideos(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE albumTitle = :albumTitle")
    fun getSongsByAlbum(albumTitle: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    // Cascade update query for Songs when Album metadata changes
    @Query("""
        UPDATE songs 
        SET albumTitle = :newAlbumTitle, albumArtist = :newAlbumArtist, genre = :newGenre, year = :newReleaseYear 
        WHERE albumTitle = :oldAlbumTitle
    """)
    suspend fun updateSongsAlbumMetadata(
        oldAlbumTitle: String,
        newAlbumTitle: String,
        newAlbumArtist: String,
        newGenre: String,
        newReleaseYear: Int
    )

    // Album queries
    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE title = :title LIMIT 1")
    suspend fun getAlbumByTitle(title: String): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    // Playlist queries
    @Query("SELECT * FROM playlists ORDER BY title ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long

    @Delete
    suspend fun deletePlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()

    @Query("DELETE FROM playlist_items")
    suspend fun deleteAllPlaylistItems()
}

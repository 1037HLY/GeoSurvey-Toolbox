// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/TrackPointDao.kt
package com.geosurvey.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insert(point: TrackPointEntity): Long

    @Insert
    suspend fun insertAll(points: List<TrackPointEntity>)

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    fun getPointsByTrackId(trackId: String): Flow<List<TrackPointEntity>>

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    suspend fun getPointsByTrackIdSync(trackId: String): List<TrackPointEntity>

    @Query("DELETE FROM track_points WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: String)

    @Query("SELECT COUNT(*) FROM track_points WHERE trackId = :trackId")
    suspend fun getPointCount(trackId: String): Int
}

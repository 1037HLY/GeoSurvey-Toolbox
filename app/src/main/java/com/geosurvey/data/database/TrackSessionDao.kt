// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/TrackSessionDao.kt
package com.geosurvey.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TrackSessionEntity)

    @Update
    suspend fun update(session: TrackSessionEntity)

    @Delete
    suspend fun delete(session: TrackSessionEntity)

    @Query("SELECT * FROM track_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TrackSessionEntity>>

    @Query("SELECT * FROM track_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): TrackSessionEntity?

    @Query("SELECT * FROM track_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): TrackSessionEntity?
}

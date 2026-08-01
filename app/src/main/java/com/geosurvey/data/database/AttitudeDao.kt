// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/AttitudeDao.kt
package com.geosurvey.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttitudeDao {
    @Insert
    suspend fun insert(attitude: AttitudeEntity): Long

    @Query("SELECT * FROM attitude_measurements ORDER BY timestamp DESC")
    fun getAllAttitudes(): Flow<List<AttitudeEntity>>

    @Delete
    suspend fun delete(attitude: AttitudeEntity)
}

// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/TrackPointEntity.kt
package com.geosurvey.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_points")
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val altitudeAccuracy: Float,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val satelliteCount: Int,
    val hdop: Float,
    val vdop: Float,
    val pdop: Float,
    val cn0Mean: Float,
    val qualityScore: Int, // 0-100
    val isFiltered: Boolean = false
)

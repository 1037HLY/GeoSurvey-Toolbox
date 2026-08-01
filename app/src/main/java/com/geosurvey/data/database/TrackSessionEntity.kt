// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/TrackSessionEntity.kt
package com.geosurvey.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_sessions")
data class TrackSessionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val pointCount: Int = 0,
    val totalDistance: Double = 0.0,
    val isActive: Boolean = true,
    val description: String = ""
)

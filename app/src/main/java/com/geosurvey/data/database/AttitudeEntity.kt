// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/AttitudeEntity.kt
package com.geosurvey.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attitude_measurements")
data class AttitudeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val strike: Float,      // 走向
    val dip: Float,         // 倾角
    val dipDirection: Float, // 倾向
    val note: String = ""
)

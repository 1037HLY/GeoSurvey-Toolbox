// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/database/GeoSurveyDatabase.kt
package com.geosurvey.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackPointEntity::class,
        TrackSessionEntity::class,
        AttitudeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GeoSurveyDatabase : RoomDatabase() {
    abstract fun trackPointDao(): TrackPointDao
    abstract fun trackSessionDao(): TrackSessionDao
    abstract fun attitudeDao(): AttitudeDao
}

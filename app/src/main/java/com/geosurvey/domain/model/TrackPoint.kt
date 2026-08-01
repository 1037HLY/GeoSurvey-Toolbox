// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/domain/model/TrackPoint.kt
package com.geosurvey.domain.model

data class TrackPoint(
    val id: Long = 0,
    val trackId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val satelliteCount: Int,
    val hdop: Float,
    val pdop: Float,
    val qualityScore: Int
)

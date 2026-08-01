// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/domain/model/GnssData.kt
package com.geosurvey.domain.model

data class GnssData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val altitudeAccuracy: Float,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Long,
    val utcTime: String,
    val satelliteCount: Int,
    val satellites: List<SatelliteInfo>,
    val hdop: Float,
    val vdop: Float,
    val pdop: Float,
    val qualityScore: Int,
    val isValid: Boolean
)

data class SatelliteInfo(
    val svid: Int,
    val constellation: String, // GPS, GLONASS, Galileo, BeiDou, QZSS
    val cn0: Float,
    val elevation: Float,
    val azimuth: Float,
    val usedInFix: Boolean
)

enum class GnssQuality {
    EXCELLENT,    // PDOP < 2, satellites >= 8, CN0 >= 35
    GOOD,         // PDOP < 4, satellites >= 6, CN0 >= 30
    FAIR,         // PDOP < 6, satellites >= 4, CN0 >= 25
    POOR,         // PDOP < 8, satellites >= 3
    INVALID       // 其他
}

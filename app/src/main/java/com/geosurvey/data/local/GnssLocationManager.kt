// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/local/GnssLocationManager.kt
package com.geosurvey.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.geosurvey.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class GnssLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _gnssData = MutableStateFlow<GnssData?>(null)
    val gnssData: StateFlow<GnssData?> = _gnssData.asStateFlow()

    private val _satellites = MutableStateFlow<List<SatelliteInfo>>(emptyList())
    val satellites: StateFlow<List<SatelliteInfo>> = _satellites.asStateFlow()

    private val _gnssStatus = MutableStateFlow<GnssStatusEvent?>(null)
    val gnssStatus: StateFlow<GnssStatusEvent?> = _gnssStatus.asStateFlow()

    private var lastLocation: Location? = null
    private var stationaryStartTime: Long = 0
    private var isStationary = false

    // GNSS状态回调
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satList = mutableListOf<SatelliteInfo>()
            var usedCount = 0
            var totalCn0 = 0f

            for (i in 0 until status.satelliteCount) {
                val constellation = when (status.getConstellationType(i)) {
                    GnssStatus.CONSTELLATION_GPS -> "GPS"
                    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                    GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                    GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
                    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                    else -> "Other"
                }

                val cn0 = status.getCn0DbHz(i)
                if (status.usedInFix(i)) {
                    usedCount++
                    totalCn0 += cn0
                }

                satList.add(
                    SatelliteInfo(
                        svid = status.getSvid(i),
                        constellation = constellation,
                        cn0 = cn0,
                        elevation = status.getElevationDegrees(i),
                        azimuth = status.getAzimuthDegrees(i),
                        usedInFix = status.usedInFix(i)
                    )
                )
            }

            _satellites.value = satList

            // 计算DOP值（简化估算）
            val hdop = estimateHDOP(usedCount, totalCn0 / maxOf(usedCount, 1))
            val vdop = hdop * 1.5f
            val pdop = sqrt(hdop * hdop + vdop * vdop)

            _gnssStatus.value = GnssStatusEvent(
                totalSatellites = status.satelliteCount,
                usedSatellites = usedCount,
                hdop = hdop,
                vdop = vdop,
                pdop = pdop,
                cn0Mean = if (usedCount > 0) totalCn0 / usedCount else 0f
            )
        }
    }

    // 位置回调
    private val locationCallback = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            processLocation(location)
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // 注册GNSS状态监听
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback, mainHandler)
        }

        // 请求位置更新 - 使用Fused Location Provider通过系统服务
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L, // 1秒
            0f,    // 0米
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationCallback)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
        }
    }

    private fun processLocation(location: Location) {
        val status = _gnssStatus.value
        val quality = calculateQuality(status, location.accuracy)

        // 静止状态检测
        detectStationaryState(location)

        // 跳点过滤
        if (shouldFilterPoint(location, quality)) {
            return
        }

        val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        val gnssData = GnssData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            altitudeAccuracy = location.verticalAccuracyMeters,
            speed = location.speed,
            bearing = location.bearing,
            accuracy = location.accuracy,
            timestamp = location.time,
            utcTime = utcFormat.format(Date(location.time)),
            satelliteCount = status?.usedSatellites ?: 0,
            satellites = _satellites.value,
            hdop = status?.hdop ?: 99f,
            vdop = status?.vdop ?: 99f,
            pdop = status?.pdop ?: 99f,
            qualityScore = quality.score,
            isValid = quality.quality != GnssQuality.INVALID && quality.quality != GnssQuality.POOR
        )

        _gnssData.value = gnssData
        lastLocation = location
    }

    private fun calculateQuality(status: GnssStatusEvent?, accuracy: Float): QualityResult {
        if (status == null) return QualityResult(GnssQuality.INVALID, 0)

        val pdop = status.pdop
        val satCount = status.usedSatellites
        val cn0 = status.cn0Mean

        val quality = when {
            pdop < 2 && satCount >= 8 && cn0 >= 35 -> GnssQuality.EXCELLENT
            pdop < 4 && satCount >= 6 && cn0 >= 30 -> GnssQuality.GOOD
            pdop < 6 && satCount >= 4 && cn0 >= 25 -> GnssQuality.FAIR
            pdop < 8 && satCount >= 3 -> GnssQuality.POOR
            else -> GnssQuality.INVALID
        }

        val score = when (quality) {
            GnssQuality.EXCELLENT -> 95
            GnssQuality.GOOD -> 80
            GnssQuality.FAIR -> 60
            GnssQuality.POOR -> 40
            GnssQuality.INVALID -> 0
        }

        return QualityResult(quality, score)
    }

    private fun detectStationaryState(location: Location) {
        lastLocation?.let { last ->
            val distance = location.distanceTo(last)
            if (distance < 2.0f) { // 2米内认为静止
                if (stationaryStartTime == 0L) {
                    stationaryStartTime = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - stationaryStartTime > 5000) {
                    isStationary = true
                }
            } else {
                stationaryStartTime = 0
                isStationary = false
            }
        }
    }

    private fun shouldFilterPoint(location: Location, quality: QualityResult): Boolean {
        // 过滤低质量点
        if (!quality.isValid) return true

        // 静止时不记录
        if (isStationary) return true

        // 跳点检测
        lastLocation?.let { last ->
            val distance = location.distanceTo(last)
            val timeDiff = (location.time - last.time) / 1000.0
            if (timeDiff > 0) {
                val speed = distance / timeDiff
                if (speed > 50) { // 速度超过50m/s认为是跳点
                    return true
                }
            }
        }

        return false
    }

    private fun estimateHDOP(usedSatellites: Int, meanCn0: Float): Float {
        return when {
            usedSatellites >= 8 && meanCn0 >= 35 -> 1.5f
            usedSatellites >= 6 && meanCn0 >= 30 -> 2.5f
            usedSatellites >= 4 && meanCn0 >= 25 -> 4.0f
            usedSatellites >= 3 -> 6.0f
            else -> 99.0f
        }
    }

    data class GnssStatusEvent(
        val totalSatellites: Int,
        val usedSatellites: Int,
        val hdop: Float,
        val vdop: Float,
        val pdop: Float,
        val cn0Mean: Float
    )

    data class QualityResult(
        val quality: GnssQuality,
        val score: Int
    ) {
        val isValid: Boolean
            get() = quality != GnssQuality.INVALID
    }
}

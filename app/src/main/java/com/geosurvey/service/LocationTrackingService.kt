// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/service/LocationTrackingService.kt
package com.geosurvey.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geosurvey.MainActivity
import com.geosurvey.R
import com.geosurvey.data.local.GnssLocationManager
import com.geosurvey.data.repository.TrackRepository
import com.geosurvey.domain.model.TrackPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var gnssManager: GnssLocationManager

    @Inject
    lateinit var trackRepository: TrackRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackId: String? = null
    private var isTracking = false

    companion object {
        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"
        const val EXTRA_TRACK_NAME = "EXTRA_TRACK_NAME"
        const val NOTIFICATION_CHANNEL_ID = "geosurvey_tracking_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val trackName = intent.getStringExtra(EXTRA_TRACK_NAME) ?: "未命名轨迹"
                startTracking(trackName)
            }
            ACTION_STOP -> {
                stopTracking()
            }
        }
        return START_STICKY
    }

    private fun startTracking(trackName: String) {
        if (isTracking) return

        isTracking = true
        startForeground(NOTIFICATION_ID, buildNotification("正在初始化定位..."))

        serviceScope.launch {
            trackId = trackRepository.createSession(trackName)
            gnssManager.startLocationUpdates()

            gnssManager.gnssData.collect { data ->
                data?.let {
                    if (it.isValid && trackId != null) {
                        val point = TrackPoint(
                            trackId = trackId!!,
                            timestamp = it.timestamp,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            altitude = it.altitude,
                            speed = it.speed,
                            bearing = it.bearing,
                            accuracy = it.accuracy,
                            satelliteCount = it.satelliteCount,
                            hdop = it.hdop,
                            pdop = it.pdop,
                            qualityScore = it.qualityScore
                        )
                        trackRepository.addTrackPoint(point)

                        updateNotification(
                            "定位中 | 卫星:${it.satelliteCount} | 精度:${String.format("%.1f", it.accuracy)}m"
                        )
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        isTracking = false
        serviceScope.launch {
            trackId?.let { trackRepository.stopSession(it) }
        }
        gnssManager.stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "轨迹记录服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "地质勘查轨迹记录后台服务"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("GeoSurvey 轨迹记录")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = buildNotification(content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

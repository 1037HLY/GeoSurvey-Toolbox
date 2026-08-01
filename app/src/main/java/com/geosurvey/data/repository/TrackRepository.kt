// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/data/repository/TrackRepository.kt
package com.geosurvey.data.repository

import com.geosurvey.data.database.*
import com.geosurvey.domain.model.TrackPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val trackPointDao: TrackPointDao,
    private val trackSessionDao: TrackSessionDao
) {
    fun getAllSessions(): Flow<List<TrackSessionEntity>> = trackSessionDao.getAllSessions()

    suspend fun createSession(name: String): String {
        val id = System.currentTimeMillis().toString()
        val session = TrackSessionEntity(
            id = id,
            name = name,
            startTime = System.currentTimeMillis(),
            isActive = true
        )
        trackSessionDao.insert(session)
        return id
    }

    suspend fun stopSession(trackId: String) {
        val session = trackSessionDao.getSessionById(trackId)?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )
        session?.let { trackSessionDao.update(it) }
    }

    suspend fun addTrackPoint(point: TrackPoint) {
        val entity = TrackPointEntity(
            trackId = point.trackId,
            timestamp = point.timestamp,
            latitude = point.latitude,
            longitude = point.longitude,
            altitude = point.altitude,
            altitudeAccuracy = 0f,
            speed = point.speed,
            bearing = point.bearing,
            accuracy = point.accuracy,
            satelliteCount = point.satelliteCount,
            hdop = point.hdop,
            vdop = 0f,
            pdop = point.pdop,
            cn0Mean = 0f,
            qualityScore = point.qualityScore
        )
        trackPointDao.insert(entity)
    }

    fun getTrackPoints(trackId: String): Flow<List<TrackPointEntity>> = 
        trackPointDao.getPointsByTrackId(trackId)

    suspend fun deleteTrack(trackId: String) {
        trackPointDao.deleteByTrackId(trackId)
        trackSessionDao.getSessionById(trackId)?.let { trackSessionDao.delete(it) }
    }

    suspend fun getActiveSession(): TrackSessionEntity? = trackSessionDao.getActiveSession()
}

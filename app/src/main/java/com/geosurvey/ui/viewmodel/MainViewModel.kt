// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/ui/viewmodel/MainViewModel.kt
package com.geosurvey.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geosurvey.data.local.GnssLocationManager
import com.geosurvey.domain.model.GnssData
import com.geosurvey.domain.model.SatelliteInfo
import com.geosurvey.service.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val gnssManager: GnssLocationManager
) : AndroidViewModel(application) {

    val gnssData: StateFlow<GnssData?> = gnssManager.gnssData
    val satellites: StateFlow<List<SatelliteInfo>> = gnssManager.satellites

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    init {
        // 启动基础定位（不记录轨迹）
        gnssManager.startLocationUpdates()
    }

    fun startTracking(trackName: String) {
        _isTracking.value = true
        val intent = Intent(getApplication(), LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
            putExtra(LocationTrackingService.EXTRA_TRACK_NAME, trackName)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun stopTracking() {
        _isTracking.value = false
        val intent = Intent(getApplication(), LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        if (!_isTracking.value) {
            gnssManager.stopLocationUpdates()
        }
    }
}

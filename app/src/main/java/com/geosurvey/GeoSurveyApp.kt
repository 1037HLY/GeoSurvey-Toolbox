// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/GeoSurveyApp.kt
package com.geosurvey

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GeoSurveyApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

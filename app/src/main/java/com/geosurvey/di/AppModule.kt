// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/di/AppModule.kt
package com.geosurvey.di

import android.content.Context
import androidx.room.Room
import com.geosurvey.data.database.GeoSurveyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GeoSurveyDatabase {
        return Room.databaseBuilder(
            context,
            GeoSurveyDatabase::class.java,
            "geosurvey_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}

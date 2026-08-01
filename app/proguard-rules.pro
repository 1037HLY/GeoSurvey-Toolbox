# GeoSurvey-Toolbox/app/proguard-rules.pro
-keep class com.geosurvey.** { *; }
-keepclassmembers class com.geosurvey.** { *; }
-keepattributes *Annotation*
-dontwarn org.osmdroid.**

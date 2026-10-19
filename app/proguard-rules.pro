# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**
# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
# Domain models
-keep class com.visiondetect.ai.domain.model.** { *; }
-keep class com.visiondetect.ai.data.local.entity.** { *; }
# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
# Timber
-dontwarn org.jetbrains.annotations.**
-keepclasseswithmembernames class * { native <methods>; }

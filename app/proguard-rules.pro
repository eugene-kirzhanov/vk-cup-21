-keepattributes *Annotation*,Signature
-keepattributes Exceptions

# Android
-dontwarn java.lang.**
-dontwarn org.codehaus.**
-dontwarn com.google.**
-dontwarn java.nio.**
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

# AndroidX Navigation
-keep class * extends androidx.fragment.app.Fragment{}

# GMS
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-dontnote com.google.**
-keep class com.google.firebase.**
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
# Retrofit 2
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keep class retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit.**

# GSON
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-dontwarn sun.misc.**

# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn okio.BufferedSink
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Picasso
-dontnote com.squareup.**

# Mapbox
-keep class com.segment.analytics.** { *; }
-keep class com.mapbox.android.telemetry.**
-keep class com.mapbox.android.core.location.**
-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-dontnote com.mapbox.mapboxsdk.**
-dontnote com.mapbox.android.gestures.**
-dontnote com.mapbox.mapboxsdk.plugins.**

object Versions {
    const val applicationId = "by.anegin.vkcup21"
    const val versionName = "1.0.0"
    const val versionCode = 1

    const val minSdk = 21
    const val targetSdk = 30
    const val buildTools = "30.0.3"

    const val kotlin = "1.5.21"
}

object Plugins {
    const val android = "com.android.tools.build:gradle:4.2.2"
}

object Deps {

    object Coroutines {
        private const val version = "1.5.1"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val playServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0-alpha01"
        const val appCompat = "androidx.appcompat:appcompat:1.4.0-alpha03"
        const val activity = "androidx.activity:activity-ktx:1.3.0-rc02"
        const val fragment = "androidx.fragment:fragment-ktx:1.4.0-alpha04"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.0-beta02"
        const val preferences = "androidx.preference:preference:1.1.1"
        const val browser = "androidx.browser:browser:1.3.0"
    }

    object Design {
        const val material = "com.google.android.material:material:1.5.0-alpha01"
    }

    object Lifecycle {
        private const val version = "2.4.0-alpha02"
        const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
        const val java8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    }

    object Dagger {
        private const val version = "2.37"
        const val compiler = "com.google.dagger:dagger-compiler:$version"

        object Hilt {
            const val plugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
            const val core = "com.google.dagger:hilt-core:$version"
            const val android = "com.google.dagger:hilt-android:$version"
            const val compiler = "com.google.dagger:hilt-compiler:$version"

            object AndroidX {
                private const val version = "1.0.0"
                const val compiler = "androidx.hilt:hilt-compiler:$version"
                const val navigation = "androidx.hilt:hilt-navigation-fragment:$version"
            }
        }
    }

    object Navigation {
        private const val version = "2.4.0-alpha04"
        const val safeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:$version"
        const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
        const val uiKtx = "androidx.navigation:navigation-ui-ktx:$version"
        const val dynamicFeature = "androidx.navigation:navigation-dynamic-features-fragment:$version"
    }

    object PlayServices {
        const val basement = "com.google.android.gms:play-services-basement:17.6.0"
        const val location = "com.google.android.gms:play-services-location:18.0.0"
    }

    object MapBox {
        const val sdk = "com.mapbox.mapboxsdk:mapbox-android-sdk:9.6.2"
        const val turf = "com.mapbox.mapboxsdk:mapbox-sdk-turf:5.9.0-alpha.1"
        const val search = "com.mapbox.search:mapbox-search-android:1.0.0-beta.15"
        const val services = "com.mapbox.mapboxsdk:mapbox-sdk-services:5.9.0-alpha.1"
        const val navigation = "com.mapbox.navigation:ui:1.6.1"
        const val annotationPlugin = "com.mapbox.mapboxsdk:mapbox-android-plugin-annotation-v9:0.9.0"
    }

    object GoogleLibs {
        const val places = "com.google.android.libraries.places:places:2.4.0"
    }

    object Glide {
        private const val version = "4.12.0"
        const val runtime = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
    }

    object OkHttp3 {
        private const val version = "4.9.1"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Vk {
        private const val version = "3.1.0"
        const val core = "com.vk:android-sdk-core:$version"
        const val api = "com.vk:android-sdk-api:$version"
    }

    const val gson = "com.google.code.gson:gson:2.8.7"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val viewBindingPropertyDelegate = "com.github.kirich1409:viewbindingpropertydelegate:1.4.7"

}

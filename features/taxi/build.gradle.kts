plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(Versions.targetSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        kapt {
            javacOptions {
                option("-Adagger.fastInit=ENABLED")
                option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
            }
        }
    }

    buildFeatures.viewBinding = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions.jvmTarget = "1.8"

    sourceSets["main"].java.srcDir("src/main/kotlin")

    kapt {
        useBuildCache = true
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":app"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(Deps.Coroutines.playServices)

    implementation(Deps.AndroidX.preferences)

    kapt(Deps.Dagger.compiler)

    implementation(Deps.PlayServices.location)

    implementation(Deps.OkHttp3.okhttp)
    implementation(Deps.Retrofit2.retrofit)
    implementation(Deps.Retrofit2.gsonConverter)

    implementation(Deps.MapBox.sdk)
    implementation(Deps.MapBox.turf)
    implementation(Deps.MapBox.services)
    implementation(Deps.MapBox.search) {
        exclude(group = "com.mapbox.mapboxsdk", module = "mapbox-android-telemetry")
    }
    implementation(Deps.MapBox.navigation) {
        exclude(group = "com.mapbox.mapboxsdk", module = "mapbox-android-telemetry")
    }
    implementation(Deps.MapBox.annotationPlugin)
    implementation(Deps.MapBox.localizationPlugin)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
    )
}

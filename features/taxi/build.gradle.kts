plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(Versions.targetSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        applicationId = "${Versions.applicationId}.features.taxi"

        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        versionName = Versions.versionName
        versionCode = Versions.versionCode

        kapt {
            javacOptions {
                option("-Adagger.fastInit=ENABLED")
                option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
            }
        }
    }

    buildFeatures.viewBinding = true

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

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

    kapt(Deps.Dagger.compiler)
}

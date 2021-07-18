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

    kapt(Deps.Dagger.compiler)

    implementation(Deps.Vk.core) {
        exclude(group = "com.squareup.okhttp3") // moved to app-module, as used in multiple feature-module
    }
    implementation(Deps.Vk.api) {
        exclude(group = "com.google.code.gson") // moved to app-module, as used in multiple feature-module
    }
}

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

    kapt(Deps.Dagger.compiler)
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(Versions.targetSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        applicationId = Versions.applicationId
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

    dynamicFeatures = mutableSetOf(
        ":features:taxi",
        ":features:news"
    )

    signingConfigs {
        val debugProperties = loadProperties(project, ".appconfig/debug.properties")
        val releaseProperties = loadProperties(project, ".appconfig/release.properties", ".appconfig/debug.properties")

        getByName("debug") {
            storeFile = File(project.rootDir, debugProperties.getProperty("KEYSTORE_FILE"))
            storePassword = debugProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = debugProperties.getProperty("KEYSTORE_ALIAS")
            keyPassword = debugProperties.getProperty("KEYSTORE_ALIAS_PASSWORD")
        }
        create("release") {
            storeFile = File(project.rootDir, releaseProperties.getProperty("KEYSTORE_FILE"))
            storePassword = releaseProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = releaseProperties.getProperty("KEYSTORE_ALIAS")
            keyPassword = releaseProperties.getProperty("KEYSTORE_ALIAS_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
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
    implementation(kotlin("stdlib-jdk8"))

    api(Deps.Coroutines.core)
    api(Deps.Coroutines.android)

    api(Deps.AndroidX.coreKtx)
    api(Deps.AndroidX.appCompat)
    api(Deps.AndroidX.activity)
    api(Deps.AndroidX.fragment)
    api(Deps.AndroidX.constraintLayout)
    api(Deps.AndroidX.preferences)

    api(Deps.Design.material)

    api(Deps.Lifecycle.runtime)
    api(Deps.Lifecycle.viewModel)
    api(Deps.Lifecycle.liveData)
    api(Deps.Lifecycle.java8)

    api(Deps.Dagger.Hilt.android)
    kapt(Deps.Dagger.Hilt.compiler)
    kapt(Deps.Dagger.Hilt.AndroidX.compiler)

    api(Deps.Navigation.fragmentKtx)
    api(Deps.Navigation.uiKtx)
    api(Deps.Navigation.dynamicFeature)

    api(Deps.timber)

    api(Deps.viewBindingPropertyDelegate)

    implementation(Deps.PlayServices.basement)
}

fun loadProperties(project: Project, propertiesFileName: String, fallbackPropertiesFileName: String? = null) =
    File(project.rootDir, propertiesFileName).let { propertiesFile ->
        try {
            org.jetbrains.kotlin.konan.properties.loadProperties(propertiesFile.path)
        } catch (e: Exception) {
            if (fallbackPropertiesFileName != null) {
                val fallbackPropertiesFile = File(project.rootDir, fallbackPropertiesFileName)
                org.jetbrains.kotlin.konan.properties.loadProperties(fallbackPropertiesFile.path)
            } else {
                e.printStackTrace()
                throw e
            }
        }
    }

plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")

    id("dagger.hilt.android.plugin")

    id("dependencies")
}

val debugProperties = loadProperties(project, ".appconfig/debug.properties")
val releaseProperties = loadProperties(project, ".appconfig/release.properties", ".appconfig/debug.properties")

android {
    compileSdkVersion(Versions.targetSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        versionName = Versions.versionName
        versionCode = 1

        kapt {
            arguments {
                arg("dagger.fastInit", "enabled")
            }
            javacOptions {
                option("-Adagger.fastInit=ENABLED")
                option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
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
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    coreLibraryDesugaring(Deps.desugar)

    implementation(kotlin("stdlib-jdk8"))

    implementation(Deps.KotlinXSerialization.json)

    implementation(Deps.Coroutines.core)
    implementation(Deps.Coroutines.android)

    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.AndroidX.activity)
    implementation(Deps.AndroidX.fragment)
    implementation(Deps.AndroidX.recyclerView)
    implementation(Deps.AndroidX.constraintLayout)

    implementation(Deps.Design.material)

    implementation(Deps.Lifecycle.runtime)
    implementation(Deps.Lifecycle.viewModel)
    implementation(Deps.Lifecycle.liveData)
    implementation(Deps.Lifecycle.java8)

    implementation(Deps.Hilt.android)
    kapt(Deps.Hilt.compiler)
    implementation(Deps.Hilt.androidXNavigation)
    implementation(Deps.Hilt.androidXWork)
    kapt(Deps.Hilt.androidXCompiler)

    implementation(Deps.timber)

    implementation(Deps.viewBindingPropertyDelegate)
}

kapt {
    useBuildCache = true
    correctErrorTypes = true
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

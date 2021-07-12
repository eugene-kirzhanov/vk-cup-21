pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion("1.5.20")
            }
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:4.2.2")
            }
            if (requested.id.id == "dagger.hilt.android.plugin") {
                useModule("com.google.dagger:hilt-android-gradle-plugin:2.37")
            }
        }
    }
}

rootProject.name = "VkCup21"
includeBuild("buildConfig")
include(":app")

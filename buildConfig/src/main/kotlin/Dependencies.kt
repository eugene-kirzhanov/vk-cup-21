import org.gradle.api.Plugin
import org.gradle.api.Project

class DependenciesPlugin : Plugin<Project> {
    override fun apply(target: Project) {}
}

object Versions {
    const val applicationIdStandard = "by.anegin.vkcup21"
    const val versionName = "1.0.0"

    const val minSdk = 21
    const val targetSdk = 30
    const val buildTools = "30.0.3"
}

object Deps {

    object KotlinXSerialization {
        const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2"
    }

    object Coroutines {
        private const val version = "1.5.1"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0-alpha01"
        const val appCompat = "androidx.appcompat:appcompat:1.4.0-alpha03"
        const val activity = "androidx.activity:activity-ktx:1.3.0-rc01"
        const val fragment = "androidx.fragment:fragment-ktx:1.4.0-alpha04"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.0-beta02"
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

    object Room {
        private const val version = "2.4.0-alpha02"
        const val runtime = "androidx.room:room-runtime:$version"
        const val compiler = "androidx.room:room-compiler:$version"
        const val ktx = "androidx.room:room-ktx:$version"
    }

    object Hilt {
        private const val version = "2.37"
        private const val version2 = "1.0.0"
        const val plugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
        const val core = "com.google.dagger:hilt-core:$version"
        const val android = "com.google.dagger:hilt-android:$version"
        const val compiler = "com.google.dagger:hilt-compiler:$version"
        const val androidXCompiler = "androidx.hilt:hilt-compiler:$version2"
        const val androidXNavigation = "androidx.hilt:hilt-navigation-fragment:$version2"
        const val androidXWork = "androidx.hilt:hilt-work:$version2"
    }

    const val desugar = "com.android.tools:desugar_jdk_libs:1.1.5"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val viewBindingPropertyDelegate = "com.github.kirich1409:viewbindingpropertydelegate:1.4.6"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = Versions.kotlin))
        classpath(kotlin("serialization", version = Versions.kotlin))
        classpath(Plugins.android)
        classpath(Deps.Dagger.Hilt.plugin)
        classpath(Deps.Navigation.safeArgsPlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}

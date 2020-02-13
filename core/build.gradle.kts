import de.gapps.utils.configureSourcesJarTaskIfNecessary
import de.gapps.utils.getSourcesJarTask

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    `maven-publish`
}

android {
    compileSdkVersion(Android.apiLevel)
    buildToolsVersion(Android.buildTools)
    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)
        multiDexEnabled = true
        versionCode = Android.versionCode
        versionName = Android.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("androidTest").java.setSrcDirs(listOf("src/main/kotlin", "src/androidTest/kotlin"))
        getByName("androidTest").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))
    }
}

dependencies {
    with(Libs) {
        implementation(kotlin)
        implementation(coroutinesJdk)
        implementation(coroutinesSwing)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
    }

    with(Dependencies) { fullTesting() }
}

configureSourcesJarTaskIfNecessary()

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "core"
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/core-release.aar"))
            artifact(getSourcesJarTask())
        }
    }
}
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
    Libs.run {
        api(core)
        implementation(multidex)
        implementation(kotlin)
        implementation(coroutinesJdk)
        implementation(coroutinesAndroid)
        implementation(androidXappCompat)
        implementation(androidXcoreKtx)
        implementation(androidXconstraintLayout)

        implementation(testing)
        implementation(androidXtestEspressoCore)
        implementation(androidXtestRunner)
        implementation(androidXtestCore)
        implementation(androidXtestRules)

        androidTestImplementation(testing)
        androidTestImplementation(androidXtestEspressoCore)
        androidTestImplementation(androidXtestRunner)
        androidTestImplementation(androidXtestCore)
        androidTestImplementation(androidXtestRules)
        androidTestImplementation(mockWebServer)
        androidTestImplementation(bouncyCastle)
    }
}

//configurePublishing(LibraryType.AAR, "android")


configureSourcesJarTaskIfNecessary()

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "android"
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/android-release.aar"))
            artifact(getSourcesJarTask())
        }
    }
}
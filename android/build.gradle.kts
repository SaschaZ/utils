import de.gapps.utils.LibraryType.AAR
import de.gapps.utils.configurePublishing

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.0"
//    id("digital.wup.android-maven-publish")
//    id("com.kezong.fat-aar")
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
        missingDimensionStrategy("dev.zieger.utils:core", "release")
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
        implementation(androidXappCompat)
        implementation(androidXcoreKtx)
        implementation(androidXconstraintLayout)

        implementation(kotlin)
        implementation(coroutinesJdk)
        implementation(coroutinesAndroid)

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

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(AAR, "android")
import de.gapps.utils.configurePublishing
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
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    implementation("org.jetbrains.kotlin:kotlin-test:1.3.61")

    implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
    implementation("org.bouncycastle:bcprov-jdk16:1.46")
    implementation("androidx.test:rules:1.2.0")
    implementation("androidx.test:runner:1.2.0")
    implementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("androidx.test:core:1.2.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

configureSourcesJarTaskIfNecessary()

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "testing"
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/testing-release.aar"))
            artifact(getSourcesJarTask())
        }
    }
}
import de.gapps.utils.LibraryType.AAR
import de.gapps.utils.configurePublishing

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
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

// If the value is changed to true, the dependencies of the remote dependency will also be embedded in the final aar.
// the default value of transitive is false
//configurations["embed"].isTransitive = true

dependencies {
    Libs.run {
        api(core)

        implementation(kotlin)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
        implementation("org.jetbrains.kotlin:kotlin-test:1.3.61")

        implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
        implementation("org.bouncycastle:bcprov-jdk16:1.46")
        implementation("androidx.test:rules:1.2.0")
        implementation("androidx.test:runner:1.2.0")
        implementation("androidx.test.espresso:espresso-core:3.2.0")
        implementation("androidx.test:core:1.2.0")

        testImplementation(project(":core"))
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    }
}

configurePublishing(AAR, "testing")
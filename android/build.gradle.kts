import de.gapps.utils.Android
import de.gapps.utils.Dependencies
import de.gapps.utils.LibraryType.AAR
import de.gapps.utils.Libs
import de.gapps.utils.configurePublishing

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

android {
    compileSdkVersion(Android.apiLevel)
    buildToolsVersion(Android.buildTools)
    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)
        versionCode = Android.versionCode
        versionName = Android.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("test").java.setSrcDirs(listOf("src/main/kotlin", "src/test/kotlin"))
        getByName("test").assets.setSrcDirs(listOf("src/main/assets"))
    }
}

dependencies {
    Libs.run {
        implementation(core)

        implementation(androidXappCompat)
        implementation(androidXcoreKtx)
        implementation(androidXconstraintLayout)

        implementation(kotlin)
        implementation(coroutinesAndroid)

        with(Dependencies) { androidTesting() }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(AAR, "android")
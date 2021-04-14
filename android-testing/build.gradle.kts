import dev.zieger.utils.Android
import dev.zieger.utils.ModuleType.ANDROID_LIB
import dev.zieger.utils.configModule

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    kotlin("kapt")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("android-testing", ANDROID_LIB) {
    implementation(project(":android"))
    implementation(project(":core"))
    implementation(project(":core-testing"))
}

android {
    compileSdkVersion(Android.apiLevel)
    buildToolsVersion(Android.buildTools)
    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)

        setVersionCode(Android.versionCode)
        setVersionName(Android.versionName)
        setTestInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }
    lintOptions {
        isAbortOnError = false
    }

    sourceSets {
        getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))
        getByName("main").assets.setSrcDirs(listOf("src/main/assets"))

        getByName("test").java.setSrcDirs(listOf("src/main/kotlin", "src/test/kotlin"))
        getByName("test").assets.setSrcDirs(listOf("src/main/assets"))
    }
}

tasks {
    withType<Test>().configureEach {
        outputs.upToDateWhen {false}
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}
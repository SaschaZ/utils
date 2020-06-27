import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.dokka") version "0.9.17"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }

    tasks.withType(KotlinJvmCompile::class.java).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
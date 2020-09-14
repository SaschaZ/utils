buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        maven("https://kotlin.bintray.com/ktor")
        maven("https://kotlin.bintray.com/kotlinx")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.dokka") version "0.9.17"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        maven("https://kotlin.bintray.com/ktor")
        maven("https://kotlin.bintray.com/kotlinx")
    }
    tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile::class.java).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
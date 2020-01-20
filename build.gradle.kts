import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    }
}
tasks.withType<KotlinCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.suppressWarnings = true
}

plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "dev.zieger.utils"
version = "1.1.24"

allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        jcenter()
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
}
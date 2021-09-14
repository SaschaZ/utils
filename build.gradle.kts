import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")
    }
}

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.dokka") version "0.10.1"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
    tasks {
        withType<Test>().all {
            useJUnitPlatform()
            outputs.upToDateWhen { false }
        }

        withType<DokkaTask>().all {
            outputFormat = "html"
            outputDirectory = "$buildDir/javadoc"
        }

        withType<KotlinJvmCompile>().all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}
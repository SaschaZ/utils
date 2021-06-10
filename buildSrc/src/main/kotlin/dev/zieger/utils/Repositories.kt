package dev.zieger.utils

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

fun ProjectContext.configRepositories(): MavenArtifactRepository =
    repositories.run {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
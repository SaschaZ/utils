package dev.zieger.utils

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

fun ProjectContext.configRepositories(): MavenArtifactRepository =
    repositories.run {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
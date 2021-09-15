package dev.zieger.utils

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

fun ProjectContext.configRepositories(): MavenArtifactRepository =
    repositories.run {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
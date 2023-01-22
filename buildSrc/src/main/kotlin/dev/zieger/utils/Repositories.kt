package dev.zieger.utils

import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.internal.authentication.DefaultBasicAuthentication
import org.gradle.kotlin.dsl.withType

fun ProjectContext.configRepositories(): MavenArtifactRepository =
    repositories.run {
        mavenCentral()
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "ziegerDevReleases"
            setUrl("https://maven.zieger.dev/releases")
            credentials {
                username = rootProject.findProperty("mavenUser").toString()
                password = rootProject.findProperty("mavenPass").toString()
            }
            authentication {
                withType<DefaultBasicAuthentication>()
            }
        }
    }
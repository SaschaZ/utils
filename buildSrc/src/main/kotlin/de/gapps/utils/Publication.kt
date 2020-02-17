package de.gapps.utils

import Globals
import de.gapps.utils.LibraryType.AAR
import de.gapps.utils.LibraryType.JAR
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

enum class LibraryType {
    AAR,
    JAR
}

fun Project.configurePublishing(type: LibraryType, name: String) {
    when (type) {
        AAR -> configureAarPublishing(name)
        JAR -> configureJarPublishing(name)
    }
}

internal fun Project.configureJarPublishing(name: String) {
    configurePublishTask(JAR)
    configureLibraryJarPublication(name)
}

internal fun Project.configureLibraryJarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            groupId = Globals.group
            artifactId = name
            version = Globals.version

            from(components["java"])
        }
    }
}

internal fun Project.configureAarPublishing(name: String) {
    configureSourcesJarTaskIfNecessary()
    configurePublishTask(AAR)
    configureLibraryAarPublication(name)
}

internal fun Project.configureLibraryAarPublication(projectName: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            groupId = Globals.group
            artifactId = projectName
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/$projectName-release.aar"))
            artifact(getSourcesJarTask())
        }
    }
}

internal fun Project.configurePublishTask(type: LibraryType) = afterEvaluate {
    val publish = tasks["publish"]
    val assembleRelease = tasks["assemble${if (type == AAR) "Release" else ""}"]
    val publishAarPublicationToMavenLocal = tasks[if (type == AAR) "publishAarPublicationToMavenLocal"
    else "publishMavenJavaPublicationToMavenLocal"]

    publishAarPublicationToMavenLocal.dependsOn(assembleRelease)
    publish.dependsOn(assembleRelease)
}
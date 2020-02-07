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
    configureSourcesJarTaskIfNecessary()
    configureLibraryJarPublication(name)
}

internal fun Project.configureLibraryJarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            groupId = Globals.group
            artifactId = name
            version = Globals.version

            from(components["java"])
            artifact(getSourcesJarTask())

            pom.withXml {
                asNode().appendNode("dependencies").apply {
                    configurations["implementation"].dependencies.forEach { dependency ->
                        val dependencyNode = appendNode("dependency")
                        dependencyNode.appendNode(dependency, "runtime")
                    }
                    configurations["api"].dependencies.forEach { dependency ->
                        val dependencyNode = appendNode("dependency")
                        dependencyNode.appendNode(dependency)
                    }
                }
            }
        }
    }
}

internal fun Project.configureAarPublishing(name: String) {
    configureSourcesJarTaskIfNecessary()
    configurePublishTask()
    configureLibraryAarPublication(name)
}

internal fun Project.configureLibraryAarPublication(name: String) {
    val projectName = name
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            groupId = Globals.group
            artifactId = name
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/$projectName-release.aar"))
            artifact(getSourcesJarTask())

            pom.withXml {
                asNode().appendNode("dependencies").apply {
                    configurations["implementation"].dependencies.forEach { dependency ->
                        val dependencyNode = appendNode("dependency")
                        dependencyNode.appendNode(dependency, "runtime")
                    }
                    configurations["api"].dependencies.forEach { dependency ->
                        val dependencyNode = appendNode("dependency")
                        dependencyNode.appendNode(dependency)
                    }
                }
            }
        }
    }
}

internal fun Project.configurePublishTask() = afterEvaluate {
    val publish = tasks["publish"]
    val assembleRelease = tasks["assembleRelease"]
    val publishAarPublicationToMavenLocal = tasks["publishAarPublicationToMavenLocal"]

    publishAarPublicationToMavenLocal.dependsOn(assembleRelease)
    publish.dependsOn(assembleRelease)
}
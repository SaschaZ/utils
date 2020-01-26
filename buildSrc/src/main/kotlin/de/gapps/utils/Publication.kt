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
        AAR -> configureAarPublishing()
        JAR -> configureJarPublishing()
    }
}

internal fun Project.configureJarPublishing() {
    configureSourcesJarTaskIfNecessary()
    configureLibraryJarPublication()
}

internal fun Project.configureLibraryJarPublication() {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            groupId = Globals.group
            artifactId = "android"
            version = Globals.version

            from(components["java"])
            artifact(getSourcesJarTask())

//            pom.withXml {
//                asNode().appendNode("dependencies").apply {
//                    configurations["implementation"].dependencies.forEach { dependency ->
//                        val dependencyNode = appendNode("dependency")
//                        dependencyNode.appendDependency(dependency, scope = "runtime")
//                    }
//                    configurations["api"].dependencies.forEach { dependency ->
//                        val dependencyNode = appendNode("dependency")
//                        dependencyNode.appendDependency(dependency)
//                    }
//                }
//            }
        }
    }
}

internal fun Project.configureAarPublishing() {
    configureSourcesJarTaskIfNecessary()
    configurePublishTask()
    configureLibraryAarPublication()
}

internal fun Project.configureLibraryAarPublication() {
    val projectName = name
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            groupId = Globals.group
            artifactId = "android"
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/$projectName-release.aar"))
            artifact(getSourcesJarTask())

//            pom.withXml {
//                asNode().appendNode("dependencies").apply {
//                    configurations["implementation"].dependencies.forEach { dependency ->
//                        val dependencyNode = appendNode("dependency")
//                        dependencyNode.appendDependency(dependency, scope = "runtime")
//                    }
//                    configurations["api"].dependencies.forEach { dependency ->
//                        val dependencyNode = appendNode("dependency")
//                        dependencyNode.appendDependency(dependency)
//                    }
//                }
//            }
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
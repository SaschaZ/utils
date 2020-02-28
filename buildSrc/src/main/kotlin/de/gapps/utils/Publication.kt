@file:Suppress("RemoveRedundantBackticks")

package de.gapps.utils

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import de.gapps.utils.LibraryType.AAR
import de.gapps.utils.LibraryType.JAR
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

enum class LibraryType {
    AAR,
    JAR
}

fun Project.configurePublishing(type: LibraryType, name: String) {
    when (type) {
        AAR -> configureAarPublishing(name)
        JAR -> configureJarPublishing(name)
    }

//    tasks.findByName("assembleAndroidTest")?.also {
//        tasks.replace("assembleAndroidTest")
//    }
}

internal fun Project.configureJarPublishing(name: String) {
    configurePublishTask(JAR)
    configureLibraryJarPublication(name)
    addBuildDirRepository()
}

internal fun Project.configureLibraryJarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            groupId = Globals.group
            artifactId = name
            version = Globals.version

            from(components["java"])
            artifact(getSourcesJar(JAR))
            artifact(getDokkaJar(name))
        }
    }
}

internal fun Project.configureAarPublishing(name: String) {
    configurePublishTask(AAR)
    configureLibraryAarPublication(name)
    addBuildDirRepository()
}

internal fun Project.configureLibraryAarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            groupId = Globals.group
            artifactId = name
            version = Globals.version

            artifact(file("$buildDir/outputs/aar/$name-release.aar"))
            artifact(getSourcesJar(AAR))
            artifact(getDokkaJar(name))
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

internal fun Project.addBuildDirRepository() {
    repositories {
        maven {
            url = uri("$buildDir/repository")
        }
    }
}

internal fun Project.getDokkaJar(name: String): Jar {
    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "$name Utils"
        archiveClassifier.set("javadoc")
        from(tasks["dokka"])
    }
    return dokkaJar
}

private typealias AndroidBaseExtension = BaseExtension

private val Project.`sourceSets`: org.gradle.api.tasks.SourceSetContainer
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

private val Project.`androidSourceSets`: AndroidSourceSet
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByType(AndroidBaseExtension::class).sourceSets["main"] as AndroidSourceSet

internal fun Project.getSourcesJar(type: LibraryType): Any {
    if (tasks.findByName("sourcesJar") != null) return tasks["sourcesJar"]

    return tasks.register<org.gradle.api.tasks.bundling.Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(
            when (type) {
                JAR -> sourceSets["main"].allSource
                AAR -> androidSourceSets.java.srcDirs
            }
        )
    }.get()
}
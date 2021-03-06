@file:Suppress("RemoveRedundantBackticks", "UnstableApiUsage")

package dev.zieger.utils

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import dev.zieger.utils.ModuleType.ANDROID_LIB
import dev.zieger.utils.ModuleType.JVM_LIB
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

fun Project.configurePublishing(type: ModuleType, name: String) {
    when (type) {
        ANDROID_LIB -> configureAarPublishing(name)
        JVM_LIB -> configureJarPublishing(name)
    }
}

internal fun Project.configureJarPublishing(name: String) {
    configurePublishTask(JVM_LIB)
    configureLibraryJarPublication(name)
}

internal fun Project.configureLibraryJarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            configureNames(name)

            from(components["java"])
            artifact(getSourcesJar(JVM_LIB))
            artifact(getDokkaJar())
        }
    }
}

internal fun Project.configureAarPublishing(name: String) {
    configurePublishTask(ANDROID_LIB)
    configureLibraryAarPublication(name)
}

internal fun Project.configureLibraryAarPublication(name: String) {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            configureNames(name)

            artifact(file("$buildDir/outputs/aar/$name-release.aar"))
            artifact(getSourcesJar(ANDROID_LIB))
            artifact(getDokkaJar())
        }
    }
}

private fun MavenPublication.configureNames(name: String) {
    groupId = Globals.group
    artifactId = name
    version = Globals.version
}

internal fun Project.configurePublishTask(type: ModuleType) = afterEvaluate {
    val publish = tasks["publish"]
    val assemble = tasks["assemble"]
    val publishLocal = tasks["publish${
        when (type) {
            ANDROID_LIB -> "Aar"
            JVM_LIB -> "MavenJava"
        }
    }PublicationToMavenLocal"]

    publishLocal.dependsOn(assemble).doLast { copyArtifacts(type) }
    publish.dependsOn(assemble).doLast { copyArtifacts(type) }
}

internal fun Project.copyArtifacts(type: ModuleType) {
    delete(file("$rootDir/build/publications/$name/${Globals.version}"))
    copy {
        rename {
            it.replace(name, "$name-${Globals.version}")
                .replace("-release", "")
                .replace("pom-default.xml", "$name-${Globals.version}.pom")
                .replace("module.json", "$name-${Globals.version}.module")
        }
        when (type) {
            ANDROID_LIB -> from(
                file("$buildDir/outputs/aar/$name-release.aar"),
                file("$buildDir/libs"),
                file("$buildDir/publications/aar")
            )
            JVM_LIB -> from(
                file("$buildDir/libs"),
                file("$buildDir/publications/mavenJava")
            )
        }
        into(file("$rootDir/build/publications/$name/${Globals.version}"))
    }
}

internal fun Project.getDokkaJar(): Jar {
    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "$name Utils"
        archiveClassifier.set("javadoc")
        from(tasks["dokka"])
    }
    return dokkaJar
}

private typealias AndroidBaseExtension = BaseExtension

private val Project.`sourceSets`: SourceSetContainer
    get() =
        (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

private val Project.`androidSourceSets`: AndroidSourceSet
    get() =
        (this as ExtensionAware).extensions.getByType(AndroidBaseExtension::class).sourceSets["main"] as AndroidSourceSet

internal fun Project.getSourcesJar(type: ModuleType): Any {
    if (tasks.findByName("sourcesJar") != null) return tasks["sourcesJar"]

    return tasks.register<org.gradle.api.tasks.bundling.Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(
            when (type) {
                JVM_LIB -> sourceSets["main"].allSource
                ANDROID_LIB -> androidSourceSets.java.srcDirs
            }
        )
    }.get()
}
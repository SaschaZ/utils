@file:Suppress("RemoveRedundantBackticks", "UnstableApiUsage")

package dev.zieger.utils

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

fun ProjectContext.configPublishing() {
    group = packageName
    version = Globals.version

    when (isAndroid) {
        true -> configureAarPublishing()
        false -> configureJarPublishing()
    }
}

internal fun ProjectContext.configureJarPublishing() {
    configurePublishTask()
    configureLibraryJarPublication()
}

internal fun ProjectContext.configureLibraryJarPublication() {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            configureNames(moduleName)

            from(components["java"])
            artifact(getSourcesJar())
            artifact(getDokkaJar())
        }
    }
}

internal fun ProjectContext.configureAarPublishing() {
    configurePublishTask()
    configureLibraryAarPublication()
}

internal fun ProjectContext.configureLibraryAarPublication() {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("aar") {
            configureNames(moduleName)

            artifact(file("$buildDir/outputs/aar/$name-release.aar"))
            artifact(getSourcesJar())
            artifact(getDokkaJar())
        }
    }
}

private fun MavenPublication.configureNames(moduleName: String) {
    groupId = Globals.group
    artifactId = moduleName
    version = Globals.version
}

internal fun ProjectContext.configurePublishTask() = afterEvaluate {
    val publish = tasks["publish"]
    val assemble = tasks["assemble${
        when (isAndroid) {
            true -> ""//""Release"
            false -> ""
        }
    }"]
    val publishLocal = tasks["publishToMavenLocal"/*${when (isAndroid) {
        true -> "Aar"
        false -> "MavenJava"
    }}PublicationToMavenLocal"*/]

    publishLocal.dependsOn(assemble).doLast { copyArtifacts() }
    publish.dependsOn(assemble).doLast { copyArtifacts() }
}

internal fun ProjectContext.copyArtifacts() {
    delete(file("$rootDir/build/publications/$moduleName/${Globals.version}"))
    copy {
        rename {
            it.replace(moduleName, "$moduleName-${Globals.version}")
                .replace("-release", "")
                .replace("pom-default.xml", "$moduleName-${Globals.version}.pom")
                .replace("module.json", "$moduleName-${Globals.version}.module")
        }
        when (isAndroid) {
            true -> from(
                file("$buildDir/outputs/aar/$moduleName-release.aar"),
                file("$buildDir/libs"),
                file("$buildDir/publications/aar")
            )
            false -> from(
                file("$buildDir/libs"),
                file("$buildDir/publications/mavenJava")
            )
        }
        into(file("$rootDir/build/publications/$moduleName/${Globals.version}"))
    }
}

internal fun ProjectContext.getDokkaJar(): Jar {
    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "$moduleName Utils"
        archiveClassifier.set("javadoc")
//        from(tasks["dokka"])
    }
    return dokkaJar
}

val ProjectContext.`sourceSets`: SourceSetContainer
    get() =
        (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val ProjectContext.`androidSourceSets`: AndroidSourceSet
    get() = (this as ExtensionAware).extensions.getByType(AndroidBaseExtension::class).sourceSets["main"] as AndroidSourceSet

fun ProjectContext.getSourcesJar(): Any {
    if (tasks.findByName("sourcesJar") != null) return tasks["sourcesJar"]

    return tasks.register<org.gradle.api.tasks.bundling.Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(
            when (isAndroid) {
                false -> sourceSets["main"].allSource
                true -> androidSourceSets.java.srcDirs
            }
        )
    }.get()
}
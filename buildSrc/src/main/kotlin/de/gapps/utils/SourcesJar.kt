package de.gapps.utils

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

private const val sourcesJarTaskName = "sourcesJar"

private typealias AndroidBaseExtension = BaseExtension

fun Project.configureSourcesJarTaskIfNecessary() {
    if (tasks.findByName(sourcesJarTaskName) != null) return

    val extensions = extensions
    tasks.register<Jar>(sourcesJarTaskName) {
        archiveClassifier.set("sources")
        from(extensions.findByType(SourceSetContainer::class)?.let { it.findByName("main")?.java?.srcDirs }
            ?: extensions.getByType(AndroidBaseExtension::class).sourceSets["main"].java.srcDirs)
    }
}

fun Project.getSourcesJarTask(): Task = tasks.getByName(sourcesJarTaskName)
package dev.zieger.utils

import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

fun ProjectContext.configJacoco() {
    extensions.getByType(JacocoPluginExtension::class.java).run {
        toolVersion = Versions.jacocoVersion
        reportsDirectory.set(file("$buildDir/jacoco"))
    }
    tasks.register("applicationCodeCoverageReport", JacocoReport::class.java) {
        sourceSets(sourceSets.findByName("main"))
    }
    tasks.withType(JacocoReport::class.java) {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            xml.destination = File("${project.buildDir}/jacoco/jacocoTestReport.xml")
            html.destination = File("${project.buildDir}/jacoco/html")
        }
    }
}
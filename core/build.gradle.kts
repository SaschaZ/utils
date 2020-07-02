import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.Versions
import dev.zieger.utils.configModule
import dev.zieger.utils.coreTesting
import dev.zieger.utils.kotlinReflect

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
    jacoco
}

jacoco {
    toolVersion = Versions.jacoco
    reportsDir = file("$buildDir/jacoco")
}

tasks.register<JacocoReport>("applicationCodeCoverageReport") {
    sourceSets(sourceSets.main.get())
}

configModule("core", JVM_LIB) {
    coreTesting
    kotlinReflect
}

tasks {
    test {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            xml.destination = file("${buildDir}/jacoco/jacocoTestReport.xml")
            html.destination = file("${buildDir}/jacoco/html")
        }
    }
}

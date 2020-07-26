import dev.zieger.utils.*
import dev.zieger.utils.ModuleType.JVM_LIB

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
    ktorClientGson
    ktorServerGson
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

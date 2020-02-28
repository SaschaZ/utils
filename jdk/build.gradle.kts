import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing
import de.gapps.utils.Android
import de.gapps.utils.Libs
import de.gapps.utils.Dependencies

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

dependencies {
    Libs.run {
        implementation(kotlin)
        implementation(coroutinesJdk)
    }

    with(Dependencies) { kotlinJunit5() }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(JAR, "jdk-testing")

import de.gapps.utils.LibraryType.JAR
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
//    id("digital.wup.android-maven-publish")
//    id("com.kezong.fat-aar")
}

dependencies {
    Libs.run {
        implementation(kotlin)
        implementation(coroutinesJdk)
    }

    with(Dependencies) { kotlinTesting() }
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}

configurePublishing(JAR, "jdk-testing")

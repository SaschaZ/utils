import de.gapps.utils.LibraryType
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    with(Libs) {
        implementation(kotlin)
        implementation(coroutinesJdk)
        implementation(coroutinesSwing)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
    }

    with(Dependencies) { fullTesting() }
}

java {
    @Suppress("UnstableApiUsage")
    withSourcesJar()
    @Suppress("UnstableApiUsage")
    withJavadocJar()
}

configurePublishing(LibraryType.JAR, "core")
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    with(Libs) {
        implementation(core)
        implementation(kotlin)
        implementation(coroutinesJdk)

        implementation(mordant)
        implementation(progressbar)
    }
}

java {
    @Suppress("UnstableApiUsage")
    withSourcesJar()
    @Suppress("UnstableApiUsage")
    withJavadocJar()
}

configurePublishing(de.gapps.utils.LibraryType.JAR, "console")
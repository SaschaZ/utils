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

configurePublishing(de.gapps.utils.LibraryType.JAR, "console")
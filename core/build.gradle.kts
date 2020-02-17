import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    with(Libs) {
        implementation(kotlin)
        implementation(coroutinesAndroid)
        implementation(coroutinesSwing)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
    }

    with(Dependencies) { fullTesting() }
}

configurePublishing(de.gapps.utils.LibraryType.JAR, "core")
plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    with(Libs) {
        api(core)
        implementation(kotlin)
        implementation(coroutinesJdk)

        implementation(mordant)
        implementation(progressbar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "console"
            version = Globals.version

            from(components["java"])
        }
    }
}

java {
    @Suppress("UnstableApiUsage")
    withSourcesJar()
    @Suppress("UnstableApiUsage")
    withJavadocJar()
}
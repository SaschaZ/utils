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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "core"
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
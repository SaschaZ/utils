plugins {
    id("java")
    id("org.gradle.java-library")
    id("kotlin")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.gradle.jacoco")
}

dependencies {
    val kotlinCoroutinesVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    api(project(":time"))
    api(project(":misc"))
    api(project(":globals"))

    val koTestVersion: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
    testImplementation("io.kotest:kotest-property:$koTestVersion")
    val mockkVersion: String by project
    testImplementation("io.mockk:mockk:$mockkVersion")
}

afterEvaluate {
    configurePublishTask()
    configurePublishExtension()

    configJacoco()
}

fun Project.configurePublishTask() = afterEvaluate {
    val publish = tasks["publish"]
    val assemble = tasks["assembleRelease"]
    val publishLocal = tasks["publishToMavenLocalMavenJavaPublicationToMavenLocal"]

    publishLocal.dependsOn(assemble).doLast { copyArtifacts() }
    publish.dependsOn(assemble).doLast { copyArtifacts() }
}

fun configurePublishExtension() {
    extensions.getByType<PublishingExtension>().publications {
        register<MavenPublication>("mavenJava") {
            groupId = "dev.zieger.utils"
            artifactId = name
            version = extra["projectVersion"] as String

            from(components["java"])
            artifact(getSourcesJar())
            artifact(getDokkaJar(artifactId))
        }
    }
}

val sourceSets: SourceSetContainer
    get() =
        (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

fun getSourcesJar(): Any {
    if (tasks.findByName("sourcesJar") != null) return tasks["sourcesJar"]

    return tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }.get()
}

fun getDokkaJar(moduleName: String): Jar {
    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "$moduleName Utils"
        archiveClassifier.set("javadoc")
        from(tasks["dokka"])
    }
    return dokkaJar
}

fun copyArtifacts() {
    delete(file("$rootDir/build/publications/$name/$version"))
    copy {
        rename {
            it.replace(name, "$name-$version")
                .replace("-release", "")
                .replace("pom-default.xml", "$name-$version.pom")
                .replace("module.json", "$name-$version.module")
        }
        from(
            file("$buildDir/libs"),
            file("$buildDir/publications/mavenJava")
        )
        into(file("$rootDir/build/publications/$name/$version"))
    }
}

fun configJacoco() {
    extensions.getByType(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java).run {
        toolVersion = dev.zieger.utils.Versions.jacocoVersion
        reportsDirectory.set(file("$buildDir/jacoco"))
    }
    tasks.register("applicationCodeCoverageReport", org.gradle.testing.jacoco.tasks.JacocoReport::class.java) {
        sourceSets(sourceSets.findByName("main"))
    }
    tasks.withType(org.gradle.testing.jacoco.tasks.JacocoReport::class.java) {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            xml.destination = java.io.File("${project.buildDir}/jacoco/jacocoTestReport.xml")
            html.destination = java.io.File("${project.buildDir}/jacoco/html")
        }
    }
}

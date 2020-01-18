plugins {
    kotlin("jvm")
    id("maven-publish")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {}
}

group = "com.github.SaschaZ.utils"
version = "1.1.19"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.3.2")

    implementation("org.koin:koin-core:2.0.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")

    implementation("org.slf4j:slf4j-simple:1.7.29")

    implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
    implementation("org.bouncycastle:bcprov-jdk16:1.46")

    testImplementation(project(":testing"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.61")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
}

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions.jvmTarget = "1.8"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
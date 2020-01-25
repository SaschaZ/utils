plugins {
    kotlin("jvm")
    id("maven-publish")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {}
}

group = "dev.zieger.utils"
version = "1.1.41"

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.3.3")

    implementation("org.koin:koin-core:2.0.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")

    implementation("org.slf4j:slf4j-simple:1.7.29")

    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("me.tongfei:progressbar:0.8.0")


    testImplementation(project(":console"))
    testImplementation(project(":testing"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.61")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("org.koin:koin-test:2.0.1")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.61")
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
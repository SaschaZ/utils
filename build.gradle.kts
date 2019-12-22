plugins {
    kotlin("jvm") version "1.3.61"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

group = "de.gapps.utils"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")

    implementation("org.slf4j:slf4j-simple:1.7.29")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.61")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks {
    compileKotlin {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        kotlinOptions.jvmTarget = "11"
    }
}
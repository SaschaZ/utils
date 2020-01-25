plugins {
    kotlin("jvm")
    id("maven-publish")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {}
}

group = "dev.zieger.utils"
version = "1.1.41"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.3")
    implementation("org.jetbrains.kotlin:kotlin-test:1.3.61")

    implementation("com.squareup.okhttp3:mockwebserver:4.3.1")
    implementation("org.bouncycastle:bcprov-jdk16:1.46")
    implementation("androidx.test:rules:1.2.0")
    implementation("androidx.test:runner:1.2.0")
    implementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("androidx.test:core:1.2.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
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
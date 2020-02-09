import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = Globals.group
            artifactId = "testing"
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
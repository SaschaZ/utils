import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//import org.jetbrains.dokka.gradle.DokkaTask

//plugins {
//    id("org.jetbrains.dokka")
//}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion: String by project
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
    childProjects.forEach { delete(it.value.buildDir) }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://maven.zieger.dev/releases")
        maven("https://jitpack.io")
        maven("https://repo1.maven.org/maven2")
        maven(url = "https://dl.bintray.com/kotlin/dokka")
    }

    val jvmTargetVersion: String by project
    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = jvmTargetVersion
        kotlinOptions.freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    tasks.withType<JavaCompile>() {
        sourceCompatibility = jvmTargetVersion
        targetCompatibility = jvmTargetVersion
    }

    tasks.withType(Test::class.java) {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }

//    tasks.withType<DokkaTask>().configureEach {
//        outputDirectory.set(buildDir.resolve("dokka"))
//    }

    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xinline-classes")
            jvmTarget = "15"
        }
    }
}
import de.gapps.utils.configurePublishing

plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    Libs.run {
        implementation(core)
        implementation(kotlin)
        implementation(coroutinesJdk)
        implementation(coroutinesSwing)

        implementation(koin)
        implementation(jackson)
        implementation(slf4jSimple)
        implementation(mordant)
        implementation(progressbar)


        testImplementation(console)
        testImplementation(testing)
        testImplementation(kotlinTest)
        testImplementation(kotlinTestRunnerJunit5)
        testImplementation(koinTest)
        testImplementation(androidXtestCore)
        testImplementation(androidXtestExt)
        testImplementation(mockk)
        testImplementation(coroutinesCore)
        testRuntimeOnly(junitJupiterEngine)
    }
}

configurePublishing(de.gapps.utils.LibraryType.JAR, "channels")
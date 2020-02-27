import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    fun DependencyHandler.kotlinTesting() {
        Libs.apply {
            add("testImplementation", coreTesting)

            add("testImplementation", coroutinesCore)
            add("testImplementation", kotlinTest)
            add("testImplementation", kotlinTestRunnerJunit5)

            add("testImplementation", mockk)
            add("testImplementation", koinTest)

            add("testImplementation", "junit:junit:4.13")
            add("testImplementation", "org.junit.vintage:junit-vintage-engine:5.6.0")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.6.0")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-engine:5.6.0")
        }
    }

    fun DependencyHandler.androidTesting() {
        Libs.apply {
            add("testImplementation", androidXtestCore)
            add("testImplementation", androidXtestRules)
            add("testImplementation", androidXtestRunner)
            add("testImplementation", androidXtestEspressoCore)
            add("testImplementation", androidXtestExt)
        }
    }
}
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

            add("testImplementation", junitJupiterEngine)
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
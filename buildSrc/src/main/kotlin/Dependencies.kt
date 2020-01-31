import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    fun DependencyHandler.fullTesting() {
        Libs.apply {
            add("testImplementation", testing)
            add("testImplementation", kotlinTest)
            add("testImplementation", kotlinTestRunnerJunit5)
            add("testImplementation", coroutinesCore)
            add("testImplementation", koinTest)
            add("testImplementation", androidXtestCore)
            add("testImplementation", androidXtestRules)
            add("testImplementation", androidXtestRunner)
            add("testImplementation", androidXtestEspressoCore)
            add("testImplementation", androidXtestExt)
            add("testImplementation", mockk)
            add("testRuntimeOnly", junitJupiterEngine)
        }
    }
}
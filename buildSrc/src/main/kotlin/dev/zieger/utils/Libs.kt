@file:Suppress("SpellCheckingInspection", "unused")

package dev.zieger.utils

import dev.zieger.utils.Config.API
import dev.zieger.utils.Config.IMPL
import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.project

typealias MD = ModuleDependency
typealias EMD = ExternalModuleDependency
typealias DHS = DependencyHandlerScope

enum class Config(private val raw: String) {
    IMPL("implementation"),
    API("api"),
    KAPT("kapt"),
    TEST("testImplementation");

    operator fun invoke() = raw
    override fun toString() = raw
}

@Suppress("FunctionName")
fun <M : MD> TrasitiveAcztion(trans: Boolean = true) = Action<M> { isTransitive = trans }

fun DHS.addModule(name: String, config: Config = API, action: Action<MD> = Action {}): MD =
    addDependencyTo(this, config(), project(":$name"), action)

fun DHS.add(dependency: String, config: Config = IMPL, action: Action<EMD> = Action {}): EMD =
    addDependencyTo(this, config(), dependency, action)

val DHS.globalsModule: MD get() = globalsModule()
fun DHS.globalsModule(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("globals", action = action)

val DHS.coroutinesModule: MD get() = coroutines()
fun DHS.coroutines(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("coroutines", action = action)

val DHS.logModule: MD get() = logModule()
fun DHS.logModule(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("log", action = action)

val DHS.miscModule: MD get() = miscModule()
fun DHS.miscModule(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("misc", action = action)

val DHS.observablesModule: MD get() = observablesModule()
fun DHS.observablesModule(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("observables", action = action)

val DHS.stateMachineModule: MD get() = stateMachineModuile()
fun DHS.stateMachineModuile(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("statemachine", action = action)

val DHS.timeModule: MD get() = timeModule()
fun DHS.timeModule(action: Action<MD> = TrasitiveAcztion()): MD =
    addModule("time", action = action)

val DHS.kotlin: EMD get() = kotlin()
fun DHS.kotlin(config: Config = IMPL, action: Action<EMD> = Action { isTransitive = false }): EMD =
    add("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}", config, action)

val DHS.coroutinesCore: EMD get() = coroutinesCore()
fun DHS.coroutinesCore(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.coroutinesJdk: EMD get() = coroutinesJdk()
fun DHS.coroutinesJdk(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.testCoroutinesCore: EMD get() = testCoroutinesCore()
fun DHS.testCoroutinesCore(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.testCoroutinesJdk: EMD get() = testCoroutinesJdk()
fun DHS.testCoroutinesJdk(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.coroutinesAndroid: EMD get() = coroutinesAndroid()
fun DHS.coroutinesAndroid(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.testCoroutinesAndroid: EMD get() = testCoroutinesAndroid()
fun DHS.testCoroutinesAndroid(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.coroutinesSwing: EMD get() = coroutinesSwing()
fun DHS.coroutinesSwing(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.kotlinCoroutinesVersion}", config, action)

val DHS.kotlinSerialization: EMD get() = kotlinSerialization()
fun DHS.kotlinSerialization(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerializationVersion}", config, action)

val DHS.koin: EMD get() = koin()
fun DHS.koin(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("org.koin:koin-core:${Versions.koinVersion}", config, action)

val DHS.mockk: EMD get() = mockk()
fun DHS.mockk(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.mockk:mockk:${Versions.mockkVersion}", config, action)

val DHS.ktorClientGson: EMD get() = ktorClientGson()
fun DHS.ktorClientGson(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.ktor:ktor-client-gson:${Versions.ktorVersion}", config, action)

val DHS.ktorServerGson: EMD get() = ktorServerGson()
fun DHS.ktorServerGson(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.ktor:ktor-jackson:${Versions.ktorVersion}", config, action)

val DHS.oshi: EMD get() = oshi()
fun DHS.oshi(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("com.github.oshi:oshi-core:${Versions.oshiVersion}", config, action)

val DHS.koTestRunner: EMD get() = koTestRunner()
fun DHS.koTestRunner(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.kotest:kotest-runner-junit5-jvm:${Versions.kotestVersion}", config, action)

val DHS.koTestAssertions: EMD get() = koTestAssertions()
fun DHS.koTestAssertions(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.kotest:kotest-assertions-core-jvm:${Versions.kotestVersion}", config, action)

val DHS.koTestProperty: EMD get() = koTestProperty()
fun DHS.koTestProperty(config: Config = IMPL, action: Action<EMD> = TrasitiveAcztion()): EMD =
    add("io.kotest:kotest-property-jvm:${Versions.kotestVersion}", config, action)
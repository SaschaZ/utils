@file:Suppress("FunctionName")

package dev.zieger.utils.console

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

interface DI : KoinComponent

fun DI(vararg module: Module) = DiImpl(*module)

class DiImpl(vararg module: Module) : DI {

    private val kApp = koinApplication {
        modules(module {
            single<DI> { this@DiImpl }
        }, *module)
    }

    override fun getKoin(): Koin = kApp.koin
}
@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.koin

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

interface DI : KoinComponent {
    fun release()
}

/**
 * Definitions of this type are invoked when the DI is released.
 */
class DiRelease(val release: DI.() -> Unit)

fun DI(block: KoinApplication.() -> Array<Module>) = object : DI {

    private val di: DI = this

    private val kApp = koinApplication {
        modules(module { single { di } }, *block())
    }

    override fun getKoin(): Koin = kApp.koin

    override fun release() {
        getAll<DiRelease>().forEach { it.release(this) }
    }
}

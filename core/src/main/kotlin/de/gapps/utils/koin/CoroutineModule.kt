@file:Suppress("unused")

package de.gapps.utils.koin

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.IoCoroutineScope
import kotlinx.coroutines.cancel
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun coroutineModule() = module {

    single { DefaultCoroutineScope() }
    single { IoCoroutineScope() }
}

fun KoinApplication.cancelCoroutineScopes() {
    koin.apply {
        get<DefaultCoroutineScope>().cancel()
        get<IoCoroutineScope>().cancel()
    }
}

fun KoinApplication.resetCoroutineScopes() {
    koin.apply {
        get<DefaultCoroutineScope>().reset()
        get<IoCoroutineScope>().reset()
    }
}
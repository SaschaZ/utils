@file:Suppress("unused")

package de.gapps.utils.koin

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.IoCoroutineScope
import de.gapps.utils.coroutines.scope.MainCoroutineScope
import kotlinx.coroutines.cancel
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun coroutineModule() = module {

    single { MainCoroutineScope() }
    single { DefaultCoroutineScope() }
    single { IoCoroutineScope() }
}

fun KoinApplication.cancelCoroutineScopes() {
    koin.apply {
        get<MainCoroutineScope>().cancel()
        get<DefaultCoroutineScope>().cancel()
        get<IoCoroutineScope>().cancel()
    }
}

fun KoinApplication.resetCoroutineScopes() {
    koin.apply {
        get<MainCoroutineScope>().reset()
        get<DefaultCoroutineScope>().reset()
        get<IoCoroutineScope>().reset()
    }
}
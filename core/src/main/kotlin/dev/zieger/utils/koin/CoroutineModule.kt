@file:Suppress("unused")

package dev.zieger.utils.koin

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
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
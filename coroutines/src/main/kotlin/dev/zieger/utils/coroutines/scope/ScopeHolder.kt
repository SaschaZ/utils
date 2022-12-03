package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.CoroutineScope

interface ScopeHolder {
    val scope: CoroutineScope
}
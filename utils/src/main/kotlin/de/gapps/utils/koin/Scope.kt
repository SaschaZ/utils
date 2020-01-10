package de.gapps.utils.koin

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.ScopeSet


inline fun Module.scope(name: String, scopeSet: ScopeSet.() -> Unit) {
    val scope: ScopeSet = ScopeSet(named(name)).apply(scopeSet)
    declareScope(scope)
}
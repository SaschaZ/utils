package de.gapps.utils.misc.koin

import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.ScopeSet

inline fun Module.scope(name: String, scopeSet: ScopeSet.() -> Unit) {
    val scope: ScopeSet = ScopeSet(named(name)).apply(scopeSet)
    declareScope(scope)
}

inline fun <reified T> Module.single(
    name: String? = null,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = single(name?.let(::named), createdAtStart, override, definition)

inline fun <reified T> Module.factory(
    name: String? = null,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = factory(name?.let(::named), override, definition)

inline fun <reified T> Scope.get(
    name: String,
    vararg parameters: Any
): T = get(named(name)) { parametersOf(parameters) }

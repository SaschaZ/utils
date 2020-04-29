package dev.zieger.utils.koin

import dev.zieger.utils.koin.GlobalDiHolder.SINGLE_GLOBAL_DI_KEY
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T> inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
    key: String = SINGLE_GLOBAL_DI_KEY
) = object : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        getKoinComponent(key, false)!!.getKoin().get(qualifier, parameters)
}
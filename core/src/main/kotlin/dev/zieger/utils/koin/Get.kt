package dev.zieger.utils.koin

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier


inline fun <reified T> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
    key: String = GlobalDiHolder.SINGLE_GLOBAL_DI_KEY
): T = getKoinComponent(key, false)!!.kApp.koin.get(qualifier, parameters)


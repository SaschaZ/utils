package de.gapps.utils.coroutines.scope

import kotlinx.coroutines.Dispatchers


open class MainCoroutineScope(
    scopeName: String = ICoroutineScopeEx.DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Main)
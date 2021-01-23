package dev.zieger.utils

import dev.zieger.utils.core_testing.TestCoroutineDispatcher
import dev.zieger.utils.coroutines.scope.MainCoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class TestMainCoroutineScope : MainCoroutineScope() {
    override val coroutineContext: CoroutineContext = TestCoroutineDispatcher() + Job()
}
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.coroutines.scope.UnconfinedCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * [CoroutineScope] with a dispatcher that will execute the [Runnable] directly on the same thread without any delay.
 *
 * Should only be used in tests.
 */
class TestCoroutineScope : ICoroutineScopeEx {

    override suspend fun cancelAndJoin() = Unit
    override suspend fun join() = Unit
    override fun reset() = Unit

    override val coroutineContext: CoroutineContext
        get() = Job() + object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
        }
}

class TestCoroutineDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
}

open class TestDefaultCoroutineScope : DefaultCoroutineScope() {
    override val coroutineContext: CoroutineContext = TestCoroutineDispatcher() + Job()
}

open class TestIoCoroutineScope : IoCoroutineScope() {
    override val coroutineContext: CoroutineContext = TestCoroutineDispatcher() + Job()
}

open class TestUnconfinedCoroutineScope : UnconfinedCoroutineScope() {
    override val coroutineContext: CoroutineContext = TestCoroutineDispatcher() + Job()
}
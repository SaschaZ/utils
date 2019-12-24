@file:Suppress("unused")

package de.gapps.utils

import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext


infix fun <T> T.equals(expected: T) = kotlin.test.assertEquals(expected, this)
infix fun <T> T.notEquals(expected: T) = kotlin.test.assertNotEquals(expected, this)

class TestCoroutineScope : ICoroutineScopeEx {

    override suspend fun cancelAndJoin() = Unit
    override suspend fun join() = Unit
    override fun reset() = Unit

    override val coroutineContext: CoroutineContext
        get() = Job() + object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                block.run()
            }
        }
}
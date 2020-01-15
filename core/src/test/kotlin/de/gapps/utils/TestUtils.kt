@file:Suppress("unused")

package de.gapps.utils

import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

infix fun <T> T.withText(text: String) = this to text

infix fun <T> T.equals(expected: T) = equals(expected to null)
infix fun <T> T.notEquals(expected: T) = notEquals(expected to null)
infix fun <T> T.equals(expected: Pair<T, String?>) = kotlin.test.assertEquals(expected.first, this, expected.second)
infix fun <T> T.notEquals(expected: Pair<T, String?>) =
    kotlin.test.assertNotEquals(expected.first, this, expected.second)

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
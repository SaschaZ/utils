@file:Suppress("unused")

package de.gapps.utils

import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

open class TestScopeTest : AnnotationSpec() {

    protected lateinit var scope: CoroutineScope

    @Before
    fun before() {
        scope = TestCoroutineScope()
    }

    @After
    fun after() {
        scope.cancel()
    }
}
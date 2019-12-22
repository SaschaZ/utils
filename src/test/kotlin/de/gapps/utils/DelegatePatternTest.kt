@file:Suppress("unused")

package de.gapps.utils.machineex

import io.kotlintest.specs.AnnotationSpec
import kotlin.test.assertEquals

class DelegatePatternTest : AnnotationSpec() {

    interface TestInterface {
        fun foo(): String = boo()
        fun boo(): String
    }

    class TestDelegate1 : TestInterface {
        override fun foo() = super.foo() + "foo"
        override fun boo() = "boo"
    }

    class TestParent1 : TestInterface by TestDelegate1() {
        override fun foo() = super.foo() + "hoo"
    }

    class TestParent2(private val delegate: TestInterface = TestDelegate1()) : TestInterface by delegate {
        override fun foo() = delegate.foo() + "doo"
    }

    @Test
    fun testParentOverwrite() {
        assertEquals("boohoo", TestParent1().foo())
        assertEquals("boofoodoo", TestParent2().foo())
    }
}
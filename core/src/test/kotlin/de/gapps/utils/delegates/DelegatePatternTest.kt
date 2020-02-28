@file:Suppress("unused")

package de.gapps.utils.delegates

import de.gapps.utils.core_testing.assertion.assert
import org.junit.jupiter.api.Test

class DelegatePatternTest {

    interface TestInterface {
        fun foo(): String = boo()
        fun boo(): String
    }

    class TestDelegate1 : TestInterface {
        override fun foo() = super.foo() + "2"
        override fun boo() = "1"
    }

    class TestParent1 : TestInterface by TestDelegate1() {
        override fun foo() = super.foo() + "3"
    }

    class TestParent2(private val delegate: TestInterface = TestDelegate1()) : TestInterface by delegate {
        override fun foo() = delegate.foo() + "3"
    }

    @Test
    fun testParent1() {
        TestParent1().foo() assert "13"
    }

    @Test
    fun testParent2() {
        TestParent2().foo() assert "123"
    }

    interface IParent {
        fun foo() = 1
    }

    open class Parent : IParent {
        override fun foo() = 2
    }

    class Child : Parent() {
        override fun foo() = 3
    }

    @Test
    fun testExtend() {
        val child = Child()
        val parent = child as Parent
        val iParent = child as IParent
        iParent.foo() assert 3
        parent.foo() assert 3
        child.foo() assert 3
    }
}
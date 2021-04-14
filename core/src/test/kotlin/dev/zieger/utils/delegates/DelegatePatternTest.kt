@file:Suppress("unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.core_testing.assertion.assert
import io.kotest.core.spec.style.FunSpec


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

interface IParent {
    fun foo() = 1
}

open class Parent : IParent {
    override fun foo() = 2
}

class Child : Parent() {
    override fun foo() = 3
}

class DelegatePatternTest : FunSpec({

    test("test parent 1") {
        TestParent1().foo() assert "13"
    }

    test("test parent 2") {
        TestParent2().foo() assert "123"
    }

    test("test extend") {
        val child = Child()
        val parent = child as Parent
        val iParent = child as IParent
        iParent.foo() assert 3
        parent.foo() assert 3
        child.foo() assert 3
    }
})
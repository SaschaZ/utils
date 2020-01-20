@file:Suppress("unused")

package de.gapps.utils.delegates

import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface BaseFoo {
    fun foo()
}

class Foo : BaseFoo {
    override fun foo() = print("foo")
}

interface BaseBoo {
    fun boo()
}

class Boo : BaseBoo {
    override fun boo() = print("boo")
}

interface BaseFooBoo : BaseFoo, BaseBoo
class FooBoo : BaseFooBoo {
    override fun foo() = print("foo")
    override fun boo() = print("boo")
}

interface BaseMoo {
    fun moo() = print("moo")
}

interface BaseWoo {
    fun woo() = print("woo")
}

interface BaseMooWoo : BaseMoo, BaseWoo

class MooWoo : BaseMooWoo {
    override fun moo() = print("moo")
    override fun woo() = print("woo")
}

class FooMooWoo : BaseFooWoo, BaseMooWoo by MooWoo(), BaseFoo by Foo() {
    override fun woo() {
    }
}

interface BaseAll : BaseFooBoo, BaseMooWoo
class All : BaseAll, BaseFoo by Foo(), BaseMooWoo by MooWoo(), BaseBoo by Boo()

interface BaseFooWoo {
    fun foo() = print("1")
    fun woo() = print("A")
}

class FooWoo : BaseFooWoo {
    override fun foo() {
        super.foo(); print("2")
    }

    override fun woo() {
        super.woo(); print("B")
    }
}

class OtherFooBoo(private val delegate: BaseFooWoo = FooWoo()) : BaseFooWoo by delegate {
    override fun foo() {
        super.foo();print("3")
    }

    override fun woo() {
        delegate.woo(); print("C")
    }
}

fun main() = OtherFooBoo().run { foo(); woo() }

object DelegateDemo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking { main() }
}

object Test2 {

    class CheapOnChanged<V>(
        initial: V,
        private val listener: (V) -> Unit
    ) : ReadWriteProperty<Any, V> {
        private var internal: V = initial
            set(value) {
                if (value != field) {
                    field = value; listener(value)
                }
            }

        override operator fun getValue(thisRef: Any, property: KProperty<*>): V = internal
        override operator fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
            internal = value
        }
    }

    var value: String by CheapOnChanged("foo") {
        println("changes to $it")
    }

    fun main() {
        Test2.value = "hello?"
        println("after set: ${Test2.value}")
    }

    @JvmStatic
    fun main(args: Array<String>) = main()
}

class Test3 {
    var foo: Int by object : ObservableProperty<Int>(0) {}
}

val foo1 by lazy { 0 }
var foo2 by Delegates.observable(0) { property, oldValue, newValue -> }
var foo3 by Delegates.vetoable(0) { property, oldValue, newValue -> true }
var foo4 by Delegates.notNull<Int>()
var foo5 by object : ObservableProperty<Int>(0) {
    override fun afterChange(property: KProperty<*>, oldValue: Int, newValue: Int) = Unit
    override fun beforeChange(property: KProperty<*>, oldValue: Int, newValue: Int) = true
}


data class MapTest(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(MapTest(mapOf("name" to "Peter", "age" to 40)))
        }
    }
}


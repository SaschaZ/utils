package dev.zieger.utils.log

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.lastOrNull
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds

/**
 * Log-Filter
 */
interface ILogElements {

    var elements: List<ILogElement>

    operator fun plusAssign(element: ILogElement) {
        elements = elements + element
    }

    operator fun plusAssign(element: List<ILogElement>) {
        elements = elements + element
    }

    operator fun plusAssign(element: ILogElements) {
        elements = elements + element.elements
    }

    operator fun plus(element: ILogElement?): ILogElements =
        LogElements(element?.let { elements + it } ?: elements)

    fun ILogElements.copy(elements: List<ILogElement> = this.elements.map { it.copy() }): ILogElements
}

open class LogElements(override var elements: List<ILogElement> = emptyList()) : ILogElements {

    constructor(element: ILogElement) : this(listOf(element))

    override fun ILogElements.copy(elements: List<ILogElement>): ILogElements = LogElements(elements)
}

operator fun ILogElement.unaryPlus(): List<ILogElement> = listOf(this)
operator fun ILogElement.plus(elements: ILogElements): List<ILogElement> = +this + elements.elements

interface ILogElement {

    fun ILogMessageContext.log(action: ILogMessageContext.() -> Unit)

    fun copy(): ILogElement
}

object LogLevelElement : ILogElement {

    override fun ILogMessageContext.log(action: ILogMessageContext.() -> Unit) =
        if (minLogLevel <= level) action() else Unit

    override fun copy(): ILogElement = LogLevelElement
}

interface IResetScope {
    fun reset()
}

data class SpamFilter(
    val minInterval: IDurationEx = 1.milliseconds,
    val id: String,
    val sameMessage: Boolean = false,
    val resetScope: IResetScope.() -> Unit = {}
) : ILogElement, IResetScope {

    companion object {
        private val idTimeMap = HashMap<String, ITimeEx>()
        private val idMessageMap = HashMap<String, FiFo<String>>()

        private const val MESSAGE_FIFO_SIZE = 128
    }

    override fun ILogMessageContext.log(action: ILogMessageContext.() -> Unit) {
        resetScope()

        idMessageMap.getOrPut(id) { FiFo(MESSAGE_FIFO_SIZE) }.also { fifo ->
            if (fifo.lastOrNull() == message) {
                if (sameMessage) return
            } else fifo.put(message)
        }

        when {
            idTimeMap[id]?.let { createdAt - it >= minInterval } != false -> {
                idTimeMap[id] = createdAt
                action()
            }
            else -> Unit
        }
    }

    override fun reset() = idTimeMap.remove(id).asUnit()

    override fun copy(): ILogElement = SpamFilter(minInterval, "${id}Copy", sameMessage, resetScope)
}

class ExternalFilter(val block: ILogMessageContext.() -> Boolean) : ILogElement {

    constructor(fixed: Boolean) : this({ fixed })

    override fun ILogMessageContext.log(action: ILogMessageContext.() -> Unit) =
        if (!block()) action() else Unit

    override fun copy(): ILogElement = ExternalFilter(block)
}
package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.*

interface ActionDsl : MachineDslRoot {

    /**
     *
     */
    infix fun EventCondition.set(state: AbsStateType?): Unit = execAndSet { state }

    infix fun StateCondition.fire(event: AbsEventType?): Unit = execAndFire { event }

    /**
     *
     */
    infix fun Condition.exec(block: suspend IMatchScope.() -> Unit) =
        processor.addCondition(this) { block(); null }.asUnit()

    infix fun EventCondition.execAndSet(block: suspend IMatchScope.() -> AbsStateType?) =
        processor.addCondition(this, block).asUnit()

    infix fun StateCondition.execAndFire(block: suspend IMatchScope.() -> AbsEventType?) =
        processor.addCondition(this, block).asUnit()
}
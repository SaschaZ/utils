@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*


abstract class MachineDsl : IMachineEx {

    protected val mapper: IMachineExMapper = MachineExMapper()

    // start entry with unary +
    operator fun Master.unaryPlus() = condition()
    operator fun ComboElement.unaryPlus() = Condition(this)

    // link wanted items with + operator
    operator fun Condition.plus(other: Master): Condition = plus(other.combo)
    operator fun Condition.plus(other: suspend MatchScope.() -> Boolean): Condition = apply { all += External(other) }

    operator fun Condition.plus(other: ComboElement): Condition = apply { any += other }

    // link unwanted items with - operator
    operator fun Condition.minus(other: Master): Condition = minus(other.combo)
    operator fun Condition.minus(other: suspend MatchScope.() -> Boolean): Condition = apply { none += External(other) }

    operator fun Condition.minus(other: ComboElement): Condition = apply { none += other }


    // apply Data with * operator
    operator fun Event.times(slave: Slave): ComboEventElement = comboEvent.also { it.slave = slave }
    operator fun State.times(slave: Slave): ComboStateElement = comboState.also { it.slave = slave }
    operator fun EventGroup<*>.times(slave: Slave): ComboEventGroupElement = comboEventGroup.also { it.slave = slave }
    operator fun StateGroup<*>.times(slave: Slave): ComboStateGroupElement = comboStateGroup.also { it.slave = slave }
    operator fun ComboEventElement.times(slave: Slave): ComboEventElement = also { it.slave = slave }
    operator fun ComboStateElement.times(slave: Slave): ComboStateElement = also { it.slave = slave }
//    operator fun ComboBaseElement<*, Slave>.times(slave: Slave) = also { it.slave = slave }
    operator fun PrevElement.times(slave: Slave): PrevElement = apply { combo.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: Slave) = apply {
        @Suppress("UNCHECKED_CAST")
        (start as? ComboBaseElement<Master, Slave>)?.slave = slave
    }

    operator fun Condition.plus(other: PrevElement): Condition = apply { all += other }
    operator fun Condition.minus(other: PrevElement): Condition = apply { none += other }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [State] instead.
     */
    operator fun Master.get(idx: Int): PrevElement = this[idx..idx]
    operator fun Master.get(range: IntRange): PrevElement = PrevElement(combo, range)

    /**
     * Use the [not] operator to ignore any slaves for this element.
     */
//    operator fun Master.not(): ComboElement = !combo
    operator fun Event.not(): ComboEventElement = !comboEvent
    operator fun EventGroup<out Event>.not(): ComboEventGroupElement = !comboEventGroup
    operator fun State.not(): ComboStateElement = !comboState
    operator fun StateGroup<out State>.not(): ComboStateGroupElement = !comboStateGroup
    operator fun <T: ComboBaseElement<*, *>> T.not(): T = apply { ignoreSlave = true }
    operator fun PrevElement.not(): PrevElement = apply { combo.ignoreSlave = true }

    /**
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = mapper.addBinding(this, machine)

    /**
     *
     */
    suspend infix fun Condition.set(state: IResultState) = execAndSet { state }

    /**
     *
     */
    suspend infix fun Condition.exec(block: suspend MatchScope.() -> Unit) = execAndSet { block(); null }

    /**
     *
     */
    suspend infix fun <T : IResultState> Condition.execAndSet(block: suspend MatchScope.() -> T?) {
        mapper.addCondition(this) {
            when (val result = block()) {
                is State -> result.comboState
                is ComboStateElement -> result
                null -> null
                else -> throw IllegalArgumentException("Only State abd ComboElement is allowed as return in an action.")
            }
        }
    }

    suspend infix fun <T : IResultEvent> Condition.execAndFire(block: suspend MatchScope.() -> T?) {
        exec {
            block()?.also {
                when (it) {
                    is Event -> fire eventSync it
                    is ComboEventElement -> fire eventSync it
                }
            }
        }
    }
}
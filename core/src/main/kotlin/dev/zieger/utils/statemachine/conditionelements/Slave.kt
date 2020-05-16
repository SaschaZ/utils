package dev.zieger.utils.statemachine.conditionelements

interface ISlave : IConditionElement

/**
 * Type that can be combined added to [Master].
 */
abstract class Slave : ConditionElement(), ISlave
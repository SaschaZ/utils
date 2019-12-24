package de.gapps.utils.ta.input

import de.gapps.utils.ta.input.config.*
import de.gapps.utils.ta.processing.timeval.ITimeValProcessor
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.STimeVal
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

sealed class IndicatorIdentifier<I : IOhclVal, V : Any, out T : IPlotConfig<I, V>, out O : Any, out P : ITimeValProcessor<I, O>> {

    abstract val clazz: KClass<@UnsafeVariance P>
    abstract val subId: Int
    abstract val config: T

    @Suppress("UNCHECKED_CAST")
    fun newIndicatorInstance(): P = clazz.primaryConstructor?.call(config)
        ?: throw IllegalStateException("No primary constructor found for clazz $clazz")

    data class RAW<I : IOhclVal, O : Any, out T : ITimeValProcessor<I, O>>(
        override val clazz: KClass<@UnsafeVariance T>,
        override val config: RawValueConfig<I, O>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, O, RawValueConfig<I, O>, O, T>()

    data class MA<I : IOhclVal, T : ITimeValProcessor<I, Double>>(
        override val clazz: KClass<T>,
        override val config: MaConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, Double, MaConfig<I>, Double, T>()

    data class EMA<I : IOhclVal, T : ITimeValProcessor<I, Double>>(
        override val clazz: KClass<T>,
        override val config: EmaConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, Double, EmaConfig<I>, Double, T>()

    data class BOLLINGER<I : IOhclVal, O : STimeVal.TripleLineValue, T : ITimeValProcessor<I, O>>(
        override val clazz: KClass<T>,
        override val config: BollingerBandsConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, I, BollingerBandsConfig<I>, O, T>()

    data class RSI<I : IOhclVal, T : ITimeValProcessor<I, Double>>(
        override val clazz: KClass<T>,
        override val config: RsiConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, Double, RsiConfig<I>, Double, T>()

    data class TD<I : IOhclVal, T : ITimeValProcessor<I, Int>>(
        override val clazz: KClass<T>,
        override val config: TdSequentialConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, Double, TdSequentialConfig<I>, Int, T>()

    data class PARABOLIC<I : IOhclVal, T : ITimeValProcessor<I, Double>>(
        override val clazz: KClass<T>,
        override val config: ParabolicStarConfig<I>,
        override val subId: Int = 0
    ) : IndicatorIdentifier<I, I, ParabolicStarConfig<I>, Double, T>()
}
package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondArithmetic

interface ITimeEx : IMillisecondArithmetic<ITimeEx, TimeEx>, StringConverter

fun min(a: ITimeEx, b: ITimeEx): ITimeEx = if (a < b) a else b
fun max(a: ITimeEx, b: ITimeEx): ITimeEx = if (a > b) a else b


package de.gapps.utils.time.values

import de.gapps.utils.time.base.IMillisecondHolder

interface ITimeVal<out T> : IMillisecondHolder {

    val value: T
}
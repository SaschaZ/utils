package de.gapps.utils.json

import de.gapps.utils.time.values.ITimeVal


interface DeSerializer<T : Any> {

    fun serialize(value: T): String?
    fun serialize(value: List<T>): String?
    fun deserialize(value: String): List<T>?
}

interface TimeValDeSerializer<T : Any> : DeSerializer<ITimeVal<T>>
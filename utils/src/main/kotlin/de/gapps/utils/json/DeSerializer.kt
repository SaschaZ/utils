package de.gapps.utils.json


interface DeSerializer<T : Any> {

    fun serialize(value: T): String?
    fun serialize(value: List<T>): String?
    fun deserialize(value: String): List<T>?
}
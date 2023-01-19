package dev.zieger.utils.time

sealed class TimeFormat(val pattern: String) {

    object COMPLETE : TimeFormat("d.M.yyyy-HH:mm:ss")
    object COMPLETE_ZERO : TimeFormat("dd.MM.yyyy-HH:mm:ss")
    object DATE_ONLY : TimeFormat("dd.MM.yyyy")
    object TIME_ONLY : TimeFormat("HH:mm:ss.SSS")
    object HaM : TimeFormat("HH:mm")
    object PLOT : TimeFormat("yyyy-MM-dd HH:mm:ss")
    object FILENAME : TimeFormat("yyyy-MM-dd-HH-mm-ss")
    object FILENAME_DATE : TimeFormat("yyyy-MM-dd")
    object FILENAME_TIME : TimeFormat("HH-mm-ss")
    object EXCHANGE : TimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    open class CUSTOM(pattern: String) : TimeFormat(pattern)
}
package dev.zieger.utils.coroutines.channel.network

interface Identifiable {

    companion object {

        const val NO_ID = "NO_ID"
    }

    val id: String
}
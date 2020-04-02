package dev.zieger.utils.coroutines.channel.network

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex


interface INode : Identifiable {

    val ports: IPortContainer
    var host: IHost
    val scope: CoroutineScope
    val mutex: Mutex?

    suspend fun ProcessingScope.process()

    fun portForId(id: String) = ports.portForId(id)
}

inline fun <reified T : Any> INode.inputForId(id: String) = ports.inputForId<T>(id)
    ?: throw IllegalArgumentException("Input for id $id not found.")

inline fun <reified T : Any> INode.outputForId(id: String) = ports.outputForId<T>(id)
    ?: throw IllegalArgumentException("Output for id $id not found.")

inline fun <reified T : Any> INode.ioPutForId(id: String) = ports.ioPutForId<T>(id)
    ?: throw IllegalArgumentException("IoPut for id $id not found.")

open class Node(
    override val ports: IPortContainer,
    override val id: String,
    updateType: EUpdateType = EUpdateType.COMPLETE_INPUT_CHANGE,
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val mutex: Mutex? = null,
    private val p: suspend ProcessingScope.() -> Unit = {}
) : INode {

    init {
        @Suppress("LeakingThis")
        ports.updateType = updateType
        @Suppress("LeakingThis")
        ports.onPortUpdated = {
            ProcessingScope(this@Node).process()
        }
    }

    override suspend fun ProcessingScope.process() = p()

    override lateinit var host: IHost
}


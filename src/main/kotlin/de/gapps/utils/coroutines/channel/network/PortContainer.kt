package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.network.Identifiable.Companion.NO_ID
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex

interface IPortContainer {

    val inputs: HashMap<String, Port.Input<Any>>
    val outputs: HashMap<String, Port.Output<Any>>
    val ioPuts: HashMap<String, Port.IoPut<Any>>
    val ports: HashMap<String, Port<Any>>

    val scope: CoroutineScope
    val mutex: Mutex?

    var updateType: EUpdateType
    var onPortUpdated: suspend () -> Unit
    val latestOnPortUpdated
        get() = onPortUpdated
    val updateMutex: Mutex

    fun portForId(id: String) = ports[id]

    @Suppress("EXPERIMENTAL_API_USAGE")
    val hasActiveInput: Boolean
        get() = inputs.values.any { !it.isClosedForReceive } || ioPuts.values.any { !it.isClosedForReceive }

    @Suppress("EXPERIMENTAL_API_USAGE")
    val hasAtLeastSingleContent: Boolean
        get() = inputs.values.any { !it.isEmpty } || ioPuts.values.any { !it.isEmpty }

    @Suppress("EXPERIMENTAL_API_USAGE")
    val hasFullContent: Boolean
        get() = inputs.values.all { !it.isEmpty } && ioPuts.values.all { !it.isEmpty }

    fun initPortListening() {
        scope.launchEx(mutex = mutex, interval = 10.milliseconds) {
            when {
                !hasActiveInput -> cancel()
                hasAtLeastSingleContent && updateType == EUpdateType.SINGLE_INPUT_CHANGE
                        || hasFullContent && updateType == EUpdateType.COMPLETE_INPUT_CHANGE -> latestOnPortUpdated
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> IPortContainer.inputForId(id: String) = inputs[id] as? Port.Input<T>?

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> IPortContainer.outputForId(id: String) = outputs[id] as? Port.Output<T>?

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> IPortContainer.ioPutForId(id: String) = ioPuts[id] as? Port.IoPut<T>?

open class PortContainer(
    override val inputs: HashMap<String, Port.Input<Any>> = HashMap(),
    override val outputs: HashMap<String, Port.Output<Any>> = HashMap(),
    override val ioPuts: HashMap<String, Port.IoPut<Any>> = HashMap(),
    override var updateType: EUpdateType = EUpdateType.SINGLE_INPUT_CHANGE,
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val mutex: Mutex? = null,
    override var onPortUpdated: suspend () -> Unit = {}
) : IPortContainer {

    constructor(
        inputs: List<Port.Input<*>> = emptyList(),
        outputs: List<Port.Output<*>> = emptyList(),
        ioPuts: List<Port.IoPut<*>> = emptyList(),
        updateType: EUpdateType = EUpdateType.SINGLE_INPUT_CHANGE,
        scope: CoroutineScope = DefaultCoroutineScope(),
        mutex: Mutex? = null,
        onPortUpdated: suspend () -> Unit = {}
    ) : this(
        hashMapOf(*inputs.map { it.id to it }.toTypedArray()),
        hashMapOf(*outputs.map { it.id to it }.toTypedArray()),
        hashMapOf(*ioPuts.map { it.id to it }.toTypedArray()),
        updateType, scope, mutex, onPortUpdated
    )

    override val updateMutex: Mutex = Mutex()

    init {
        @Suppress("LeakingThis")
        if (inputs.any { it.key == NO_ID }
            || outputs.any { it.key == NO_ID }
            || ioPuts.any { it.key == NO_ID }) throw IllegalArgumentException("Invalid port id used (NO_ID)")

        @Suppress("LeakingThis")
        initPortListening()
    }

    @Suppress("LeakingThis")
    override val ports: HashMap<String, Port<Any>> = hashMapOf(
        *inputs.map { it.key to it.value }.toTypedArray(),
        *outputs.map { it.key to it.value }.toTypedArray(),
        *ioPuts.map { it.key to it.value }.toTypedArray()
    )
}
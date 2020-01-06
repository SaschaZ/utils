package de.gapps.utils.coroutines.channel.network

data class ProcessingScope(
    val node: INode,
    val host: IHost = node.host,
    val updateType: EUpdateType = node.ports.updateType,
    val inputs: HashMap<String, Port.Input<*>> = node.ports.inputs,
    val outputs: HashMap<String, Port.Output<*>> = node.ports.outputs,
    val ioPuts: HashMap<String, Port.IoPut<*>> = node.ports.ioPuts
)
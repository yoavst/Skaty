@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.serialization.SimpleWriter

interface IProtocol<K : IProtocol<K>> {
    /**
     * The marker is the static extension for the protocol.
     */
    @Exclude
    val marker: IProtocolMarker<K>

    @Exclude
    var parent: IProtocol<*>?

    /**
     * Returns header size in bytes
     */
    fun headerSize(): Int

    /**
     * Write the protocol's data to the [writer] for the given [stage].
     *
     * [Stage.Data]: in: start :: out: end
     * [Stage.Length]: in: start :: out: end
     * [Stage.Checksum]: in: end :: out: start
     *
     */
    fun write(writer: SimpleWriter, stage: Stage)
}

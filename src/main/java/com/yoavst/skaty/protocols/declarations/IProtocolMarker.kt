package com.yoavst.skaty.protocols.declarations

import com.yoavst.skaty.serialization.SimpleReader

/**
 * Static extension for the protocol [K].
 */
interface IProtocolMarker<K : IProtocol<K>> : IProtocolParser<K> {
    /**
     * Return whether or not [protocol] is [K]
     *
     * suggested implementation: `protocol is K`
     */
    fun isProtocol(protocol: IProtocol<*>): Boolean

    /**
     * The name of the protocol, used for help
     */
    val name: String

    /**
     * Immutable version of the defaultValue value of the protocol.
     * Changing fields inside the defaultValue value has undefined behavior
     */
    val defaultValue: K

    override operator fun invoke(reader: SimpleReader): K = of(reader) ?: throw IllegalArgumentException("reader does not represent $name protocol.")
}

package com.yoavst.skaty.protocols

/**
 * Static extension for the protocol [K].
 */
interface IProtocolMarker<K : IProtocol<K>> {
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
}

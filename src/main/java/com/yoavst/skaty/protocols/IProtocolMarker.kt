package com.yoavst.skaty.protocols

import com.yoavst.skaty.field.Field
import kotlin.reflect.KProperty

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
     * All the fields of the protocol
     */
    val fields: Set<KProperty<Field<*>>>
}

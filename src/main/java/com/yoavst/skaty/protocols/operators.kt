package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.impl.Raw

operator fun <K : IContainerProtocol<*>> K.div(protocol: IProtocol<*>): K {
    val payload = this.payload
    if (payload == null) this.payload = protocol
    else if (payload !is IContainerProtocol<*>) throw IllegalArgumentException("cannot put payload to non container layer. Layer is: ${payload.marker.name}")
    else payload / protocol
    return this
}

operator fun <K : IContainerProtocol<*>> K.div(load: String): K = div(Raw(load))
operator fun IProtocol<*>?.contains(protocol: IProtocolMarker<*>): Boolean {
    if (this == null) return false
    if (protocol.isProtocol(this)) return true
    else if (this !is IContainerProtocol<*>) return false
    return protocol in payload
}

@Suppress("UNCHECKED_CAST")
operator fun <K : IProtocol<K>> IProtocol<*>?.get(protocol: IProtocolMarker<K>): K {
    if (this == null) throwNotFound(protocol)
    if (protocol.isProtocol(this)) return this as K
    else if (this !is IContainerProtocol<*>) throwNotFound(protocol)
    else return payload[protocol]
}

@Suppress("NOTHING_TO_INLINE")
inline fun throwNotFound(protocol: IProtocolMarker<*>): Nothing = throw IllegalArgumentException("Protocol ${protocol.name} is not found")
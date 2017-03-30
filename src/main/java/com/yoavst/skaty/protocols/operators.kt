package com.yoavst.skaty.protocols

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaMethod

operator fun <K : IContainerProtocol<*>> K.div(protocol: IProtocol<*>): K {
    val payload = this.payload
    if (payload == null) this.payload = protocol
    else if (payload !is IContainerProtocol<*>) throw IllegalArgumentException("cannot put payload to non container layer. Layer is: ${payload.marker.name}")
    else payload / protocol
    return this
}

operator fun <K : IContainerProtocol<*>> K.div(load: String): K = div(Raw(load.toByteArray()))
operator fun IProtocol<*>?.contains(protocol: IProtocolMarker<*>): Boolean {
    if (this == null) return false
    if (protocol.isProtocol(this)) return true
    else if (this !is IContainerProtocol<*>) return false
    return protocol in payload
}

@Suppress("UNCHECKED_CAST")
operator fun <K : IProtocol<K>> IProtocol<*>?.get(protocol: IProtocolMarker<K>): K {
    return getOrNull(protocol) ?: throw IllegalArgumentException("Protocol ${protocol.name} is not found")
}

@Suppress("UNCHECKED_CAST")
fun <K : IProtocol<K>> IProtocol<*>?.getOrNull(protocol: IProtocolMarker<K>): K? {
    if (this == null) return null
    if (protocol.isProtocol(this)) return this as K
    else if (this !is IContainerProtocol<*>) return null
    else return payload[protocol]
}

fun <T> del(property: KMutableProperty0<T>) {
    property.set(property.default())
}

@Suppress("UNCHECKED_CAST")
fun <T> KProperty<T>.default(): T {
    return getter.javaMethod!!.invoke(((this as? CallableReference)?.boundReceiver as? IProtocol<*>)?.marker?.defaultValue) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> KProperty<Any?>.default(instance: IProtocol<*>): T {
    return getter.javaMethod!!.invoke(instance.marker.defaultValue) as T
}
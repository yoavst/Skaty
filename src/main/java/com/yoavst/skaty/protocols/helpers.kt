package com.yoavst.skaty.protocols

import com.yoavst.skaty.utils.Help

typealias IProtocol<K> = com.yoavst.skaty.protocols.interfaces.IProtocol<K>
typealias IContainerProtocol<K> = com.yoavst.skaty.protocols.interfaces.IContainerProtocol<K>
typealias IProtocolMarker<K> = com.yoavst.skaty.protocols.interfaces.IProtocolMarker<K>

fun mac(address: String): Ether.MAC = Ether.MAC(address.toMacAddress())
fun ip(address: String): IP.Address {
    return IP.Address(address.toIpAddress())
}

fun ls(protocol: IProtocolMarker<*>) = println(Help.generate(protocol))
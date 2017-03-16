package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.declarations.IProtocolMarker
import com.yoavst.skaty.utils.Help
import com.yoavst.skaty.model.Options
import com.yoavst.skaty.serialization.readUint
import com.yoavst.skaty.serialization.readUshort
import com.yoavst.skaty.protocols.declarations.IProtocol as OrgIProtocol

typealias IProtocol<K> = OrgIProtocol<K>
typealias IProtocolMarker<K> = com.yoavst.skaty.protocols.declarations.IProtocolMarker<K>
typealias IContainerProtocol<K> = com.yoavst.skaty.protocols.declarations.IContainerProtocol<K>
typealias Layer2 = com.yoavst.skaty.protocols.declarations.Layer2
typealias Layer3 = com.yoavst.skaty.protocols.declarations.Layer3
typealias Layer4 = com.yoavst.skaty.protocols.declarations.Layer4

fun mac(address: String): Ether.MAC = Ether.MAC(address.toMacAddress())

fun mac(address: ByteArray): Ether.MAC {
    val int = address.readUint()
    val short = address.readUshort(index = 4)
    return Ether.MAC((int.toUlong() shl 16) or short.toUlong())
}

fun ip(address: String): IP.Address {
    return IP.Address(address.toIpAddress())
}

fun ip(address: ByteArray) : IP.Address {
    return IP.Address(address.readUint())
}

fun ls(protocol: IProtocolMarker<*>) = println(Help.generate(protocol))


// kotlin type alias bug, uses original class
fun <K : OrgIProtocol<K>> optionsOf(vararg options: K) = Options(options.toList())
fun <K : OrgIProtocol<K>> emptyOptions() = optionsOf<K>()
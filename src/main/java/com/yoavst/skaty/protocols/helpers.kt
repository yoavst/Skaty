package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.declarations.IProtocolMarker
import com.yoavst.skaty.utils.Help
import com.yoavst.skaty.model.Options
import com.yoavst.skaty.pcap.Pcap
import com.yoavst.skaty.serialization.*
import java.io.File
import kotlin.reflect.KClass
import com.yoavst.skaty.protocols.declarations.IProtocol as OrgIProtocol
import com.yoavst.skaty.protocols.declarations.IProtocolOption as OrgIProtocolOption

typealias IProtocol<K> = OrgIProtocol<K>
typealias IProtocolOption<K> = OrgIProtocolOption<K>
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

fun pcapOf(path: String): Pcap? = pcapOf(File(path))

fun pcapOf(file: File): Pcap? = Pcap.of(BufferSimpleReader(file.readBytes()))

fun <K : OrgIProtocol<K>> IProtocolMarker<K>.of(raw: Raw, serializationContext: SerializationContext = DefaultSerializationEnvironment): K? {
    return of(ByteArraySimpleReader(raw.load), serializationContext)
}

fun ls(protocol: IProtocolMarker<*>) = println(Help.generate(protocol))

fun IProtocol<*>.toByteArray(serializationContext: SerializationContext = DefaultSerializationEnvironment): ByteArray {
    val array = ByteArray(65535)
    val writer = ByteArraySimpleWriter(array)
    serializationContext.serialize(this, writer, SerializationContext.Stage.Data)
    return array.sliceArray(0 until writer.maxIndex)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified K: OrgIProtocolOption<in K>> lsOption() = println(Help.optionGenerate(K::class as KClass<IProtocolOption<*>>))

// kotlin type alias bug, uses original class
fun <K : OrgIProtocol<K>> optionsOf(vararg options: K) = Options(options.toMutableList())
fun <K : OrgIProtocol<K>> emptyOptions() = optionsOf<K>()
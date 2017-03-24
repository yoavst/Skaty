package com.yoavst.skaty.protocols

import com.yoavst.skaty.utils.Help
import com.yoavst.skaty.model.Options
import com.yoavst.skaty.pcap.Pcap
import com.yoavst.skaty.serialization.*
import java.io.File
import java.net.InetAddress
import kotlin.reflect.KClass

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

fun ip(address: InetAddress): IP.Address {
    return ip(address.address)
}

fun pcapOf(path: String): Pcap? = pcapOf(File(path))

fun pcapOf(file: File): Pcap? = Pcap.of(BufferSimpleReader(file.readBytes()))

fun <K : IProtocol<K>> IProtocolMarker<K>.of(raw: Raw, serializationContext: SerializationContext = DefaultSerializationEnvironment): K? {
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
inline fun <reified K: IProtocolOption<in K>> lsOption() = println(Help.optionGenerate(K::class as KClass<IProtocolOption<*>>))

// kotlin type alias bug, uses original class
fun <K : IProtocol<K>> optionsOf(vararg options: K) = Options(options.toMutableList())
fun <K : IProtocol<K>> emptyOptions() = optionsOf<K>()
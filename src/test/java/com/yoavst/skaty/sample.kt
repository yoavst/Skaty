package com.yoavst.skaty

import com.yoavst.skaty.model.flagsOf
import com.yoavst.skaty.network.Network
import com.yoavst.skaty.network.Network.send
import com.yoavst.skaty.network.Network.sendp
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.protocols.TCP.Flag.ACK
import com.yoavst.skaty.protocols.TCP.Flag.SYN
import com.yoavst.skaty.serialization.ByteArraySimpleReader
import com.yoavst.skaty.serialization.DefaultSerializationEnvironment
import unsigned.ub
import unsigned.ui
import unsigned.us
import java.io.File

fun main(args: Array<String>) {
    Network.init()

    val packets = Network.sniff(timeout = 5000).take(10).toList()
    packets.forEach(::println)

    Network.close()
    println("done")

    DefaultSerializationEnvironment.bind(TCP::dport, 3232, TestProtocol)
    DefaultSerializationEnvironment.bind(TCP::sport, 3232, TestProtocol)
}

fun testSending() {
    val packet = IP(dst = ip("192.168.1.1"), options = optionsOf(IPOption.MTUProb(22.us))) / UDP(sport = 7000.us, dport = 7000.us) / "Hello world"
    send(packet)
}

fun testSerializing() {
    val packet = Ether() /
            IP(dst = ip("192.168.1.1"), tos = 53.ub, options = optionsOf(IPOption.MTUProb(22.us))) /
            UDP(sport = 7000.us, dport = 7000.us) /
            "Hello world"

    val raw = packet.toByteArray()

    File("results.bin").writeBytes(raw)
    val p = Ether.of(ByteArraySimpleReader(File("results.bin").readBytes()))
    println(p)

}

fun testReadingPcap() {
    val sniff = pcapOf(testResourceOf("sample.pcap"))
    println(sniff)
}

fun testReadingSinglePacket() {
    val data = hexStringToByteArray(testResourceOf("sample1.bin").readText())
    val packet = Ether.of(ByteArraySimpleReader(data))
    println(packet)
}

fun showcase() {
    Network.init()

    val packet = Ether(dst = mac("11-22-33-44-55-66")) /
            IP(dst = ip("192.168.1.1"), tos = 53.ub, options = optionsOf(IPOption.MTUProb(22.us))) /
            TCP(dport = 80.us, sport = 1200.us, flags = flagsOf(SYN, ACK), options = optionsOf(TCPOption.NOP, TCPOption.Timestamp(1489416311.ui, 1.ui))) /
            "Hello world"

    ls(IP)

    // work with properties
    packet.dst = mac("AA-BB-CC-DD-EE-FF")
    del(packet::dst)
    println(packet)

    val p2 = packet.copy(type = 1.us)

    // get a layer
    val tcp = packet[TCP]
    println(tcp.dport)
    println(UDP in packet)

    // send packet
    sendp(packet)

    // sniff
    val packets = Network.sniff(timeout = 2000).filter { TCP in it && it[TCP].dport == 1200.us }.take(10).toList()
    packets.forEach(::println)
}

//region Utils
fun testResourceOf(file: String): File = File("src/test/resources/$file")

fun hexStringToByteArray(s: String): ByteArray {
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

fun <K : IProtocol<*>> Sequence<K>.dropRaw(): Sequence<K> = onEach {
    var last: IProtocol<*>? = it
    while (last is IContainerProtocol<*>) {
        if (last.payload is Raw) {
            last.payload = null
            break
        } else
            last = last.payload
    }
}
//endregion


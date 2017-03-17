package com.yoavst.skaty

import com.yoavst.skaty.model.flagsOf
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.protocols.TCP.Flag.ACK
import com.yoavst.skaty.protocols.TCP.Flag.SYN
import com.yoavst.skaty.serialization.ByteArraySimpleReader
import com.yoavst.skaty.network.Network
import unsigned.ub
import unsigned.ui
import unsigned.us
import java.io.File

fun main(args: Array<String>) {
    Network.init("192.168.1.106")

    Network.sniff(timeout = 3000).filter { TCP in it }.dropRaw().forEach(::println)

    Network.close()
    println("done")
}

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

fun testReadingPcap() {
    val sniff = pcapOf(testResourceOf("http.pcap"))
    println(sniff)
}

fun testReadingSinglePacket() {
    val data = hexStringToByteArray(testResourceOf("sample1.bin").readText())
    val packet = Ether.of(ByteArraySimpleReader(data))
    println(packet)
}

fun showcase() {
    val packet = Ether(src = mac("11-22-33-44-55-66")) /
            IP(dst = ip("192.168.1.1"), tos = 53.ub, options = optionsOf(IPOption.MTUProb(22.us))) /
            TCP(dport = 80.us, sport = 1200.us, flags = flagsOf(SYN, ACK), options = optionsOf(TCPOption.NOP, TCPOption.Timestamp(1489416311.ui, 1.ui)), chksum = 53432.us) /
            "Hello world"

    ls(IP)

    // work with properties
    packet.dst = mac("AA-BB-CC-DD-EE-FF")
    del(packet::dst)
    println(packet)

    // get a layer
    val tcp = packet[TCP]
    println(tcp.dport)
    println(UDP in packet)

    // send packet
    sendp(packet)
    val ip = packet[IP]
    send(ip)
    val response = sr1(ip, timeout = 2000)
    if (response != null) {
        println(response[Raw])
    }

    // sniff
    val packets = Network.sniff(timeout = 2000).filter { TCP in it && it[TCP].dport == 1200.us }.take(10).map { item -> item[TCP].ack }.toList()
    packets.forEach(::println)
}

fun sendp(packet: Layer2) {}
fun send(packet: Layer3) {}
fun sr1(packet: Layer3, timeout: Long = 1000): IProtocol<*>? = packet as IProtocol<*>
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


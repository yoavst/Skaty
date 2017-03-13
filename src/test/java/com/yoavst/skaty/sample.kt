package com.yoavst.skaty

import com.yoavst.skaty.model.flagsOf
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.protocols.impl.*
import com.yoavst.skaty.protocols.impl.TCP.Flag.*
import com.yoavst.skaty.protocols.impl.TCP.Companion.optionsOf
import com.yoavst.skaty.protocols.impl.TCP.Option.Companion.NOP
import unsigned.us

fun main(args: Array<String>) {
    val packet = Ether(src = mac("11-22-33-44-55-66")) /
            IP(dst = ip("192.168.1.1")) /
            TCP(dport = 80.us, sport = 1200.us, flags = flagsOf(SYN, ACK), options = optionsOf(NOP()))  /
            "Hello world"

    // get a layer
    val tcp = packet[TCP]
    println(tcp.dport())
    println(UDP in packet)

    // send packet
    sendp(packet)
    val ip = packet[IP]
    send(ip)
    val response = sr1(ip, timeout = 2000)
    if (response != null) {
        println(response)
    }

    // sniff
    val packets = sniff().filter { TCP in it && it[TCP].dport() == 1200.us }.timeout(2000).take(10).map { item -> item[TCP].ack() }.toList()
    packets.forEach(::println)

}

fun sendp(packet: Layer2) {}
fun send(packet: Layer3) {}
fun sr1(packet: Layer3, timeout: Long = 1000): IProtocol<*>? = TODO()
fun sniff(): Sequence<IProtocol<*>> = TODO()
fun <K : IProtocol<*>> Sequence<K>.timeout(timeout: Long): Sequence<K> = TODO()


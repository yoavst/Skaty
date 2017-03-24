package com.yoavst.skaty.pcap

import com.yoavst.skaty.protocols.declarations.IProtocol as IOrgProtocol
import com.yoavst.skaty.protocols.*
import unsigned.Uint

data class PcapPacket(val packet: IProtocol<*>, val time: Uint, val micros: Uint, val packetFileSize: Uint, val originalSize: Uint) {
    operator fun invoke() = packet

    operator fun contains(protocol: IProtocolMarker<*>) = protocol in packet
    operator fun <K : IOrgProtocol<K>> get(protocol: IProtocolMarker<K>) = packet[protocol]
    fun <K : IOrgProtocol<K>> getOrNull(protocol: IProtocolMarker<K>) = packet.getOrNull(protocol)
}
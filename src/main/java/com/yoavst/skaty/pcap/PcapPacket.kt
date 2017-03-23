package com.yoavst.skaty.pcap

import com.yoavst.skaty.protocols.IProtocol
import unsigned.Uint

data class PcapPacket(val packet: IProtocol<*>, val time: Uint, val micros: Uint, val packetFileSize: Uint, val originalSize: Uint) {
    operator fun invoke() = packet
}
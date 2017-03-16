@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.UshortHexFormatter
import com.yoavst.skaty.protocols.declarations.IProtocol
import com.yoavst.skaty.protocols.declarations.IProtocolMarker
import com.yoavst.skaty.protocols.declarations.Layer4
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.utils.ToString
import mu.KLogging
import unsigned.Ushort
import unsigned.us

data class UDP(var sport: Ushort = 53.us,
               var dport: Ushort = 53.us,
               var len: Ushort? = null,
               @property:Formatted(UshortHexFormatter::class) var chksum: Ushort? = null,
               override var payload: IProtocol<*>? = null) : BaseProtocol<UDP>(), IP.Aware, Layer4 {
    override fun onPayload(ip: IP) {
        ip.proto = IP.Protocol.UDP
    }

    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion

    companion object : IProtocolMarker<UDP>, KLogging() {
        override val name: String get() = "UDP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is UDP
        override val defaultValue: UDP = UDP()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): UDP? = try {
            val sport = reader.readUshort()
            val dport = reader.readUshort()
            val len = reader.readUshort()
            val checksum = reader.readUshort()
            val udp = UDP(sport, dport, len, checksum)
            udp.payload = serializationContext.serialize(reader, udp)

            udp
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a UDP packet." }
            null
        }
    }
}
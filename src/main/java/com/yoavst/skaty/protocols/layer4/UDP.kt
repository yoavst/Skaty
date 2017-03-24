@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.UshortHexFormatter
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString
import mu.KLogging
import unsigned.Ushort
import unsigned.us

data class UDP(var sport: Ushort = 53.us,
               var dport: Ushort = 53.us,
               var len: Ushort? = null,
               @property:Formatted(UshortHexFormatter::class) var chksum: Ushort? = null,
               override var _payload: IProtocol<*>? = null,
               override var parent: IProtocol<*>? = null) : BaseProtocol<UDP>(), IP.Aware, Layer4 {

    init { onPayload() }

    override fun onPayload(ip: IP) {
        ip.proto = IP.Protocol.UDP
    }

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeUshort(sport)
                writer.writeUshort(dport)
                writer.writeUshort(0.us)
                writer.writeUshort(0.us)
            }
            Stage.Length -> {
                len = (writer.maxIndex - writer.index).us
                writer.skip(4)
                writer.writeUshort(len!!)
                writer.skip(2)
            }
            Stage.Checksum -> {
                val parent = parent
                if (parent !is IP) {
                    // Checksum is not supported
                    writer.index -= headerSize()
                } else {
                    val array = ByteArray(12 + len!!.toInt())
                    val w = ByteArraySimpleWriter(array)
                    w.writeByteArray(parent.src!!.toByteArray())
                    w.writeByteArray(parent.dst.toByteArray())
                    w.writeByte(0)
                    w.writeUbyte(parent.proto)
                    w.writeUshort(len!!)
                    write(w, Stage.Data)
                    w.index -= headerSize()
                    write(w, Stage.Length)
                    w.writeByteArray(payload?.toByteArray() ?: ByteArray(0))

                    writer.index -= 2
                    chksum = calcChecksum(array).us
                    writer.writeUshort(chksum!!)
                    writer.index -= headerSize()
                }
            }
        }
    }

    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion

    override fun headerSize(): Int = 8

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
            udp.payload = serializationContext.deserialize(reader, udp)

            udp
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a UDP packet." }
            null
        }
    }
}
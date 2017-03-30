@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.*
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString
import com.yoavst.skaty.utils.clearLeftBits
import mu.KLogging
import unsigned.*

data class TCP(var sport: Ushort = 20.us,
               var dport: Ushort = 80.us,
               var seq: Uint = 0.ui,
               var ack: Uint = 0.ui,
               var dataofs: Byte? = null,
               var reserved: Byte = 0,
               var flags: Flags<Flag> = flagsOf(Flag.SYN),
               var window: Ushort = 8192.us,
               @property:Formatted(UshortHexFormatter::class) var chksum: Ushort? = null,
               var urgptr: Ushort = 0.us,
               val options: Options<TCPOption> = emptyOptions(),
               override var _payload: IProtocol<*>? = null,
               override var parent: IProtocol<*>? = null) : BaseProtocol<TCP>(), IP.Aware, Layer4 {

    init {
        onPayload()
    }

    override fun onPayload(ip: IP) {
        ip.proto = IP.Protocol.TCP
    }

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeUshort(sport)
                writer.writeUshort(dport)
                writer.writeUint(seq)
                writer.writeUint(ack)
                writer.writeUbyte(reserved.ub.clearLeftBits(5).shl(1) or (if (Flag.NS in flags) 1 else 0))
                writer.writeByte(Flag.values().filter { it in flags && it != Flag.NS }.map(Flag::value).fold(0, Int::or).toUByte())
                writer.writeUshort(window)
                writer.writeShort(0)
                writer.writeUshort(urgptr)

                var size = 0
                var current = writer.index
                options.forEach {
                    it.write(writer, stage)
                    size += writer.index - current
                    current = writer.index
                }
                while (size % 4 != 0) {
                    options += TCPOption.EndOfOptions
                    TCPOption.EndOfOptions.write(writer, Stage.Data)
                    size += 1
                }

            }
            Stage.Length -> {
                val startingIndex = writer.index
                val totalSize = headerSize()
                writer.skip(12)
                writer.writeUbyte(reserved.ub.clearLeftBits(5).shl(1) or (if (Flag.NS in flags) 1 else 0) or (totalSize / 4).shl(4))
                writer.index = startingIndex + totalSize
                dataofs = (totalSize / 4).toUByte()
            }
            Stage.Checksum -> {
                val parent = parent
                if (parent !is IP) {
                    // Checksum is not supported
                    writer.index -= headerSize()
                } else {
                    val len = (parent.len!! - parent.headerSize()).toInt()
                    val array = ByteArray(12 + len)
                    val w = ByteArraySimpleWriter(array)
                    w.writeByteArray(parent.src!!.toByteArray())
                    w.writeByteArray(parent.dst.toByteArray())
                    w.writeByte(0)
                    w.writeUbyte(parent.proto)
                    w.writeUshort(len.us)
                    write(w, Stage.Data)
                    w.index -= headerSize()
                    write(w, Stage.Length)
                    w.writeByteArray(payload?.toByteArray() ?: ByteArray(0))

                    writer.index -= 4 + optionsSize()
                    chksum = calcChecksum(array).us
                    writer.writeUshort(chksum!!)
                    writer.index -= 18
                }
            }
        }
    }

    override fun toString(): String = ToString.generate(this)

    override val marker get() = Companion

    override fun headerSize(): Int {
        val bytes = optionsSize()
        return 20 + bytes + (bytes % 4)
    }

    private fun optionsSize() = options.sumBy {
        val length = it.length.toInt()
        if (length == 0) 1 else length
    }

    companion object : IProtocolMarker<TCP>, KLogging() {
        override val name: String get() = "TCP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is TCP
        override val defaultValue: TCP = TCP()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): TCP? = try {
            val sport = reader.readUshort()
            val dport = reader.readUshort()
            val seq = reader.readUint()
            val ack = reader.readUint()

            val nextByte = reader.readUbyte()
            val headerLength = (nextByte shr 4).toByte()
            val reserved = ((nextByte shl 4) shr 5).toByte()

            val flags = mutableSetOf<Flag>()
            if ((nextByte and 1) != 0.ub) flags += Flag.NS

            val flagsByte = reader.readByte().toUInt()
            Flag.values().filterTo(flags) { it.value and flagsByte != 0 }
            val windowsSize = reader.readUshort()
            val checksum = reader.readUshort()
            val urgPtr = reader.readUshort()

            var optionsSize = (headerLength - 5) * 4
            val options = mutableListOf<TCPOption>()
            while (optionsSize > 0) {
                val option = TCPOption.of(reader, serializationContext) ?: break
                options += option
                if (option is TCPOption.EndOfOptions)
                    break
                if (option is TCPOption.NOP)
                    optionsSize -= 1
                else
                    optionsSize -= option.length
            }

            val tcp = TCP(sport, dport, seq, ack, headerLength, reserved, Flags(flags.toSet()), windowsSize, checksum,
                    urgPtr, Options(options))
            tcp.payload = serializationContext.deserialize(reader, tcp)

            tcp
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a TCP packet." }
            null
        }
    }

    //region Data objects
    enum class Flag(val value: Int) {
        FIN(0x01),
        SYN(0x02),
        RST(0x04),
        PSH(0x08),
        ACK(0x10),
        URG(0x20),
        ECE(0x40),
        CWR(0x80),
        NS(0x160)

    }
    //endregion
}



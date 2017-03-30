@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.*
import com.yoavst.skaty.network.Network
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString
import com.yoavst.skaty.utils.clearLeftBits
import mu.KLogging
import unsigned.*
import java.net.Inet4Address
import java.net.InetAddress

data class IP(var version: Byte = 4,
              var ihl: Byte? = null,
              @property:Formatted(UByteHexFormatter::class) var tos: Ubyte = 0.ub,
              var ecn: ECN = ECN.NonECT,
              var len: Ushort? = null,
              @property:Formatted(UshortHexFormatter::class) var id: Ushort = 1.us,
              var flags: Flags<Flag> = emptyFlags(),
              var ttl: Ubyte = 64.ub,
              @property:Formatted(Protocol::class) var proto: Ubyte = 0.ub,
              @property:Formatted(UshortHexFormatter::class) var chksum: Ushort? = null,
              var src: Address? = null,
              var dst: Address = ip("127.0.0.1"),
              val options: Options<IPOption> = emptyOptions(),
              override var _payload: IProtocol<*>? = null,
              override var parent: IProtocol<*>? = null) : BaseProtocol<IP>(), Ether.Aware, Layer3 {

    init { onPayload() }

    override fun onPayload() {
        (payload as? Aware)?.onPayload(this)
    }

    override fun onPayload(ether: Ether) {
        ether.type = Ether.Type.IP
    }

    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion

    override fun headerSize(): Int {
        val bytes = options.sumBy {
            val length = it.length.toInt()
            if (length == 0) 1 else length
        }
        return 20 + bytes + (bytes % 4)
    }

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeByte(version shl 4)
                writer.writeUbyte(tos shl 2 or ecn.value)
                writer.writeShort(0)
                writer.writeUshort(id)
                writer.writeUshort((Flag.values().filter { it in flags }.map(Flag::value).fold(0, Int::or) shl 15).us)
                writer.writeUbyte(ttl)
                writer.writeUbyte(proto)
                writer.writeShort(0)
                if (src == null)
                    src = Network.ipAddress
                writer.writeByteArray((src!!).toByteArray())
                writer.writeByteArray(dst.toByteArray())
                var size = 0
                var current = writer.index
                options.forEach {
                    it.write(writer, stage)
                    size += writer.index - current
                    current = writer.index
                }
                while (size % 4 != 0) {
                    options += IPOption.EndOfOptions
                    IPOption.EndOfOptions.write(writer, Stage.Data)
                    size += 1
                }
            }
            Stage.Length -> {
                val startingIndex = writer.index
                val totalSize = headerSize()
                len = (writer.maxIndex - writer.index).us
                writer.skip(2)
                writer.writeUshort(len!!)
                writer.index = startingIndex
                writer.writeByte(version shl 4 or (totalSize / 4).ub)
                ihl = (totalSize / 4).toByte()
                writer.skip(totalSize - 1)
            }
            Stage.Checksum -> {
                writer.index -= headerSize()
                val array = writer.array().copyOfRange(writer.index, writer.index + headerSize())
                writer.skip(10)
                chksum = calcChecksum(array).us
                writer.writeUshort(chksum!!)
                writer.index -= 12
            }
        }
    }

    companion object : IProtocolMarker<IP>, KLogging() {
        override val name: String get() = "IP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is IP
        override val defaultValue: IP = IP()
        override fun of(reader: SimpleReader, serializationContext: SerializationContext): IP? = try {
            val firstByte = reader.readUbyte()
            val secondByte = reader.readUbyte()

            val version = (firstByte shr 4).toByte()
            val ihl = firstByte.clearLeftBits(4).toByte()
            val tos = secondByte shr 2
            val ecn = ECN.of(secondByte.clearLeftBits(6).toInt())

            val length = reader.readUshort()
            val id = reader.readUshort()

            val forthShort = reader.readUshort()
            val flagsByte = (forthShort shr 13)

            val flags = Flag.values().filter { it.value and flagsByte != 0 }

            val ttl = reader.readUbyte()
            val proto = reader.readUbyte()
            val chksum = reader.readUshort()
            val src = Address(reader.readUint())
            val dst = Address(reader.readUint())

            var optionsSize = ihl - 5
            val options = mutableListOf<IPOption>()
            while (optionsSize > 0) {
                val option = IPOption.of(reader, serializationContext) ?: break
                options += option
                if (option is IPOption.EndOfOptions)
                    break
                if (option is IPOption.NOP)
                    optionsSize -= 1
                else
                    optionsSize -= option.length
            }

            val ip = IP(version, ihl, tos, ecn, length, id, Flags(flags.toSet()), ttl, proto, chksum, src,
                    dst, Options(options))
            ip._payload = serializationContext.deserialize(reader, ip)

            ip
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a IP packet." }
            null
        }
    }

    interface Aware {
        fun onPayload(ip: IP)
    }

    //region Data objects
    enum class Flag(val value: Int) {
        Reserved(0x1),
        DF(0x2),
        MF(0x4)
    }

    enum class ECN(val value: Int) {
        NonECT(0),
        ECT1(1),
        ECT0(2),
        CE(3);

        companion object {
            fun of(value: Int) = values().first { it.value == value }
        }
    }

    data class Address(val raw: Uint) {
        override fun toString(): String = raw.toFormattedIpAddress()

        fun toByteArray() = bufferOf(raw)

        fun toInetAddress(): InetAddress = Inet4Address.getByAddress(toByteArray())
    }

    object Protocol : Formatter<Ubyte> {
        val ICMP = 1.ub
        val IP = 4.ub
        val TCP = 6.ub
        val UDP = 17.ub
        val GRE = 47.ub

        var KnownFormats: MutableMap<Ubyte, String> = mutableMapOf(
                ICMP to "ICMP",
                IP to "IP",
                TCP to "TCP",
                UDP to "UDP",
                GRE to "GRE"
        )

        override fun format(value: Ubyte?): String = KnownFormats.getOrDefault(value ?: 0.ub, "$value")
    }
    //endregion


}

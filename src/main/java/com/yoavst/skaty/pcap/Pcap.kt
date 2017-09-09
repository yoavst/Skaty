package com.yoavst.skaty.pcap

import com.yoavst.skaty.protocols.IContainerProtocol
import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IProtocolMarker
import com.yoavst.skaty.protocols.Raw
import com.yoavst.skaty.serialization.*
import mu.KLogging
import unsigned.Uint
import unsigned.Ushort
import unsigned.ui
import unsigned.us
import java.util.*

data class Pcap(var major: Ushort = 2.us, var minor: Ushort = 4.us, var thisZone: Int = 0, var sigfigs: Uint = 0.ui, var maxSnapLen: Uint = 65536.ui,
                var dataLink: Uint = 1.ui, val packets: MutableList<PcapPacket> = mutableListOf()): IProtocol<Pcap> {
    override val marker get() = Companion
    override fun headerSize(): Int = 24
    override var parent: IProtocol<*>?
        get() = null
        set(value) {}

    override fun toString(): String {
        val counts = mutableMapOf<String, Int>()
        packets.asSequence()
                .map { it().markerName() }
                .forEach { counts[it] = counts.getOrDefault(it, 0) + 1 }

        return "<$name: ${counts.toList().joinToString(separator = " ") { (name, count) -> "$name:$count" }} />"
    }

    private fun IProtocol<*>.markerName(): String {
        return if (this is IContainerProtocol<*> && payload != null && !Raw.isProtocol(payload!!)) payload!!.markerName() else marker.name
    }

    override fun write(writer: SimpleWriter, stage: SerializationContext.Stage) {
        throw IllegalStateException("Should not call this method on pcap file")
    }

    operator fun iterator(): Iterator<PcapPacket> = packets.iterator()

    companion object : IProtocolMarker<Pcap>, KLogging() {
        override val name: String = "Pcap file"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Pcap
        override val defaultValue: Pcap = Pcap()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): Pcap? = try {
            if (reader is EndianSimpleReader) {
                reader.bigEndian()
                var magicNumber = reader.readUint().toLong()
                if (magicNumber == 0xd4c3b2a1) {
                    reader.littleEndian()
                    magicNumber = 0xa1b2c3d4
                }

                if (magicNumber != 0xa1b2c3d4)
                    throw IllegalArgumentException("source is not pcap")

                val major = reader.readUshort()
                val minor = reader.readUshort()
                require(major == 2.us && minor == 4.us) { "pcap version is not supported. Supported: 2.4" }


                val thisZone = reader.readInt() /* GMT to local correction */
                val sigfigs = reader.readUint()
                val maxSnapLen = reader.readUint()
                val dataLink = reader.readUint()

                val packets = LinkedList<PcapPacket>()
                val pcap = Pcap(major, minor, thisZone, sigfigs, maxSnapLen, dataLink, packets)
                while (reader.hasMore()) {
                    val epoch = reader.readUint()
                    val micros = reader.readUint()
                    val packetFileSize = reader.readUint() /* number of bytes of packet saved in file */
                    val originalSize = reader.readUint() /* actual length of packet */
                    if (packetFileSize != originalSize)
                        logger.warn { "mismatch! original size: $originalSize, stored: $packetFileSize" }

                    val data = reader.readByteArray(packetFileSize.toInt())
                    val result = serializationContext.deserialize(ByteArraySimpleReader(data), pcap) ?: throw Exception("Had a problem reading pcap")
                    packets += PcapPacket(result, epoch, micros, packetFileSize, originalSize)
                }
                pcap
            } else {
                throw IllegalArgumentException("reader must be endian reader")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse pcap file" }
            null
        }
    }
}
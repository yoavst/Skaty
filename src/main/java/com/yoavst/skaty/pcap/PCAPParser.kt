package com.yoavst.skaty.pcap

import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.serialization.*
import unsigned.*
import java.util.*

internal object PCAPParser {
    private val DataLink: Map<Uint, IProtocolMarker<*>> = mapOf(
            1.ui to Ether
    )

    fun parse(name: String, reader: EndianSimpleReader): SniffList {
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

        require(dataLink in DataLink) { "Currently only support ethernet. " }
        val parser = DataLink[dataLink]!!

        val packets = LinkedList<IProtocol<*>>()
        while (reader.hasMore()) {
            val epoch = reader.readUint()
            val micros = reader.readUint()
            val packetFileSize = reader.readUint() /* number of bytes of packet saved in file */
            val originalSize = reader.readUint() /* actual length of packet */
            if (packetFileSize != originalSize)
                println("mismatch! original size: $originalSize, stored: $packetFileSize")

            val data = reader.readByteArray(packetFileSize.toInt())
            val result = parser.of(ByteArraySimpleReader(data)) ?: throw Exception("Had a problem reading pcap")
            packets += result
        }
        return SniffList(name, packets)

    }
}
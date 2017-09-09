@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.clearLeftBits
import com.yoavst.skaty.utils.toInt
import mu.KLogging
import unsigned.*


sealed class IPOption(val copied: Boolean, val clazz: Class, val number: Byte, val length: Ubyte = 0.ub, @property:Exclude val data: ByteArray? = null) :
        IProtocolOption<IPOption> {
    @Exclude val name: String get() = KnownOptions[number] ?: ExtraKnownOptions[number] ?: "$number"
    open fun value(): String = if (length.toInt() in 0..2) "" else String(data!!, Charsets.US_ASCII)

    override fun equals(other: Any?): Boolean {
        return other === this || (other is IPOption && other.copied == copied && other.clazz == clazz && other.number == number &&
                other.length == length && data?.contentEquals(other.data ?: byteArrayOf()) ?: (other.data == null))
    }

    override fun hashCode(): Int {
        var result = copied.hashCode()
        result = 31 * result + clazz.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String = name + value().let { if (it.isEmpty()) it else "[$it]" }

    override fun headerSize(): Int = 2

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeUbyte((copied.toInt() shl 7 or clazz.value shl 5 or number.toInt()).ub)
                if (length.toInt() != 0) {
                    writer.writeUbyte(length)
                    if (data != null)
                        writer.writeByteArray(data)
                }
            }
            Stage.Length -> {
                writer.skip(1)
                if (length.toInt() != 0)
                    writer.skip(length.toInt() - 1 + (data?.size ?: 0))
            }
            Stage.Checksum -> {
                val length = length.toInt()
                writer.index -= if (length == 0) 1 else length
            }
        }
    }

    //region Options
    object EndOfOptions : IPOption(false, Class.Control, 0) {
        override fun headerSize(): Int = 1
    }

    object NOP : IPOption(false, Class.Control, 1) {
        override fun headerSize(): Int = 1
    }

    class Security private constructor(val level: Ushort, val compartments: Ushort, val restrictions: Ushort, val tcc: Int, data: ByteArray) :
            IPOption(true, Class.Control, 2, 11.ub, data) {

        constructor(level: Ushort = Level.Unclassified, compartments: Ushort = 0.us, restrictions: Ushort, tcc: Int) :
                this(level, compartments, restrictions, tcc, toByteArray(level, compartments, restrictions, tcc))

        constructor(data: ByteArray) : this(data.readUshort(), data.readUshort(2), data.readUshort(4), data.readInt(5).clearLeftBits(8))

        operator fun component1(): Ushort = level
        operator fun component2(): Ushort = compartments
        operator fun component3(): Ushort = restrictions
        operator fun component4(): Int = tcc

        override fun value(): String = "level=${Level.named(level)}, compartments=$compartments, restrictions=$restrictions, tcc=$tcc"

        object Level {
            val Unclassified: Ushort = 0.us
            val Confidential: Ushort = 0b1111000100110101.us
            val EFTO: Ushort = 0b0111100010011010.us
            val MMMM: Ushort = 0b1011110001001101.us
            val PROG: Ushort = 0b0101111000100110.us
            val Restricted: Ushort = 0b1010111100010011.us
            val Secret: Ushort = 0b1101011110001000.us
            val TopSecret: Ushort = 0b0110101111000101.us

            private val names: Map<Ushort, String> = mapOf(
                    Unclassified to "Unclassified",
                    Confidential to "Confidential",
                    EFTO to "EFTO",
                    MMMM to "MMMM",
                    PROG to "PROG",
                    Restricted to "Restricted",
                    Secret to "Secret",
                    TopSecret to "TopSecret"
            ).withDefault { "$it" }

            fun named(level: Ushort) = names[level]
        }

        companion object {
            private fun toByteArray(level: Ushort, compartments: Ushort, restrictions: Ushort, tcc: Int): ByteArray {
                val array = ByteArray(9)
                val writer = ByteArraySimpleWriter(array)
                writer.writeUshort(level)
                writer.writeUshort(compartments)
                writer.writeUshort(restrictions)
                writer.writeByte((tcc.shr(16)).toUByte())
                writer.writeShort((tcc.clearLeftBits(16)).toUShort())
                return array
            }
        }
    }

    open class LSSR private constructor(val pointer: Ubyte, val routeData: Array<IP.Address>, data: ByteArray, number: Byte) :
            IPOption(true, Class.Control, number, (2 + data.size).toUbyte()) {
        constructor(pointer: Ubyte, routeData: Array<IP.Address>) : this(pointer, routeData, DefaultNumber)
        constructor(data: ByteArray) : this(data, DefaultNumber)

        protected constructor(pointer: Ubyte, routeData: Array<IP.Address>, number: Byte) : this(pointer, routeData, routeData.toByteArray(pointer), number)
        protected constructor(data: ByteArray, number: Byte) : this(data.readUbyte(), data.toIpList(index = 1), data, number)

        operator fun component1(): Ubyte = pointer
        operator fun component2(): Array<IP.Address> = routeData
        override fun value(): String = "pointer=$pointer, routeData=${routeData.joinToString(prefix = "[]", postfix = "")}"

        companion object {
            private const val DefaultNumber: Byte = 3
            private fun Array<IP.Address>.toByteArray(pointer: Ubyte): ByteArray {
                val array = ByteArray(size * 4 + 1)
                array[0] = pointer.toByte()
                var i = 1
                for (address in this) {
                    val arr = address.toByteArray()
                    array[i++] = arr[0]
                    array[i++] = arr[1]
                    array[i++] = arr[2]
                    array[i++] = arr[3]
                }
                return array
            }

            private fun ByteArray.toIpList(index: Int = 0): Array<IP.Address> = Array((size - index) % 4) { IP.Address(readUint(index + it * 4)) }
        }
    }

    class RR : LSSR {
        constructor(pointer: Ubyte, routeData: Array<IP.Address>) : super(pointer, routeData, Number)
        constructor(data: ByteArray) : super(data, Number)

        companion object {
            val Number: Byte = 7
        }
    }

    class SSRR : LSSR {
        constructor(pointer: Ubyte, routeData: Array<IP.Address>) : super(pointer, routeData, Number)
        constructor(data: ByteArray) : super(data, Number)

        companion object {
            val Number: Byte = 9
        }
    }

    class StreamId private constructor(val id: Ushort, data: ByteArray) : IPOption(true, Class.Control, 8, 4.ub, data) {
        constructor(id: Ushort) : this(id, bufferOf(id))
        constructor(data: ByteArray) : this(data.readUshort(), data)

        operator fun component1(): Ushort = id
        override fun value(): String = "id=$id"
    }

    open class MTUProb private constructor(val mtu: Ushort, data: ByteArray, number: Byte) : IPOption(false, Class.Control, number, 4.ub, data) {
        constructor(mtu: Ushort) : this(mtu, DefaultNumber)
        constructor(data: ByteArray) : this(data, DefaultNumber)

        protected constructor(mtu: Ushort, number: Byte) : this(mtu, bufferOf(mtu), number)
        protected constructor(data: ByteArray, number: Byte) : this(data.readUshort(), data, number)

        operator fun component1(): Ushort = mtu
        override fun value(): String = "mtu=$mtu"

        companion object {
            private const val DefaultNumber: Byte = 11
        }
    }

    class MTUReplay : MTUProb {
        constructor(mtu: Ushort) : super(mtu, Number)
        constructor(data: ByteArray) : super(data, Number)

        companion object {
            val Number: Byte = 12
        }
    }

    class Traceroute(val id: Ushort, val outboundHops: Ushort, val returnedHops: Ushort, val originatorIP: IP.Address, data: ByteArray) :
            IPOption(false, Class.Debugging, 18, 12.ub, data) {
        constructor(id: Ushort, outboundHops: Ushort, returnedHops: Ushort, originatorIP: IP.Address) : this(id, outboundHops, returnedHops, originatorIP,
                bufferOf(id, outboundHops, returnedHops) + originatorIP.toByteArray())

        constructor(data: ByteArray) : this(data.readUshort(), data.readUshort(2), data.readUshort(4), IP.Address(data.readUint(6)), data)

        operator fun component1(): Ushort = id
        operator fun component2(): Ushort = outboundHops
        operator fun component3(): Ushort = returnedHops
        operator fun component4(): IP.Address = originatorIP

        override fun value(): String = "id=$id, outbound=$outboundHops, returned=$returnedHops, originator=$originatorIP"

    }

    class RouterAlert(val value: Ushort, data: ByteArray) : IPOption(true, Class.Control, 20, 4.ub, data) {
        constructor(value: Ushort) : this(value, bufferOf(value))
        constructor(data: ByteArray) : this(data.readUshort(), data)

        operator fun component1(): Ushort = value
        override fun value(): String = "id=$value"
    }

    class SDBM private constructor(val routeData: Array<IP.Address>, data: ByteArray) : IPOption(true, Class.Control, 21, (2 + data.size).toUbyte()) {
        constructor(routeData: Array<IP.Address>) : this(routeData, routeData.toByteArray())
        constructor(data: ByteArray) : this(data.toIpList(), data)

        operator fun component1(): Array<IP.Address> = routeData
        override fun value(): String = "routeData=${routeData.joinToString(prefix = "[]", postfix = "")}"

        companion object {
            private const val DefaultNumber: Byte = 3
            private fun Array<IP.Address>.toByteArray(): ByteArray {
                val array = ByteArray(size * 4)
                var i = 0
                for (address in this) {
                    val arr = address.toByteArray()
                    array[i++] = arr[0]
                    array[i++] = arr[1]
                    array[i++] = arr[2]
                    array[i++] = arr[3]
                }
                return array
            }

            private fun ByteArray.toIpList(index: Int = 0): Array<IP.Address> = Array((size - index) % 4) { IP.Address(readUint(index + it * 4)) }
        }
    }

    class Other(copied: Boolean, clazz: Class, number: Byte, length: Ubyte = 0.ub, data: ByteArray? = null) : IPOption(copied, clazz, number, length, data)
    //endregion

    override val marker get() = Companion

    companion object : IProtocolMarker<IPOption>, KLogging() {
        override val name: String = "IPOption"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is IPOption
        override val defaultValue: IPOption = NOP

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): IPOption? = try {
            val type = reader.readUbyte()
            when (type) {
                0.ub -> EndOfOptions
                1.ub -> NOP
                else -> {
                    val length = reader.readUbyte()
                    val data = reader.readByteArray(length.toInt() - 2)
                    generifyOf(Other((type.shr(7)).toInt() != 0, Class.of((type.clearLeftBits(2).shr(4)).toInt()),
                            type.clearLeftBits(3).toByte(), length, data))
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a IPOption packet." }
            null
        }

        fun generifyOf(option: Other): IPOption {
            val number = option.number.toInt()
            val data = option.data ?: ByteArray(0)
            return when (number) {
                0 -> EndOfOptions
                1 -> NOP
                2 -> Security(data)
                3 -> LSSR(data)
                7 -> RR(data)
                8 -> StreamId(data)
                9 -> SSRR(data)
                11 -> MTUProb(data)
                12 -> MTUReplay(data)
                18 -> Traceroute(data)
                20 -> RouterAlert(data)
                21 -> SDBM(data)
                else -> option
            }
        }

        private val KnownOptions: Map<Byte, String> = mapOf(
                0.toByte() to "end_of_list",
                1.toByte() to "nop",
                2.toByte() to "security",
                3.toByte() to "loose_source_route",
                4.toByte() to "timestamp",
                5.toByte() to "extended_security",
                6.toByte() to "commercial_security",
                7.toByte() to "record_route",
                8.toByte() to "stream_id",
                9.toByte() to "strict_source_route",
                10.toByte() to "experimental_measurement",
                11.toByte() to "mtu_probe",
                12.toByte() to "mtu_reply",
                13.toByte() to "flow_control",
                14.toByte() to "access_control",
                15.toByte() to "encode",
                16.toByte() to "imi_traffic_descriptor",
                17.toByte() to "extended_IP",
                18.toByte() to "traceroute",
                19.toByte() to "address_extension",
                20.toByte() to "router_alert",
                21.toByte() to "selective_directed_broadcast_mode",
                23.toByte() to "dynamic_packet_state",
                24.toByte() to "upstream_multicast_packet",
                25.toByte() to "quick_start",
                30.toByte() to "rfc4727_experiment"
        )

        private val ExtraKnownOptions: MutableMap<Byte, String> = mutableMapOf()

        fun addKnownOption(type: Byte, name: String) {
            if (type !in KnownOptions && type !in ExtraKnownOptions) {
                ExtraKnownOptions[type] = name
            }
        }
    }

    enum class Class(val value: Int) {
        Control(0),
        Reserved1(1),
        Debugging(2),
        Reserved3(3);

        companion object {
            fun of(value: Int) = values().first { it.value == value }
        }
    }
}
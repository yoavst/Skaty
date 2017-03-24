@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IProtocolMarker
import com.yoavst.skaty.protocols.IProtocolOption
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString
import mu.KLogging
import unsigned.*

/**
 * Options have up to three fields:
 * - Option-Kind (1 byte) - indicates type of option
 * - Option-Length (1 byte, optional) -  indicates the total length of the option
 * - Option-Data (variable, optional) - contains the value of the option if applicable
 *
 * # Examples
 * - An Option-Kind byte of 0x01 indicates that this is a No-Op option used only for padding,
 * and does not have an Option-Length or Option-Data byte following it.
 *
 * - An Option-Kind byte of 0x00 is the End Of Options option, and is also only one byte.
 *
 * **Note:** length includes Option-Kind and Option-length in its value.
 *
 */
sealed class TCPOption(val kind: Ubyte, val length: Ubyte = 0.ub, val data: ByteArray? = null) : IProtocolOption<TCPOption> {
    @Exclude val name: String get() = KnownOptions[kind] ?: ExtraKnownOptions[kind] ?: "$kind"
    open fun value(): String = if (length.toInt() in 0..2) "" else String(data!!, Charsets.US_ASCII)

    override fun equals(other: Any?): Boolean {
        return other === this || (other is TCPOption && other.kind == kind && other.length == length &&
                data?.contentEquals(other.data ?: byteArrayOf()) ?: (other.data == null))
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String = name + value().let { if (it.isEmpty()) it else "[$it]" }

    override fun headerSize(): Int = 2

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeUbyte(kind)
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
    object EndOfOptions : TCPOption(0.ub) {
        override fun headerSize(): Int = 1
    }

    object NOP : TCPOption(1.ub) {
        override fun headerSize(): Int = 1
    }

    class MaxSegSize private constructor(val size: Ushort, data: ByteArray) : TCPOption(2.ub, 4.ub, data) {
        constructor(data: ByteArray) : this(data.readUshort(), data)
        constructor(size: Ushort) : this(size, bufferOf(size))

        operator fun component1(): Ushort = size

        override fun value(): String = "size=$size"
    }

    class ShiftCount private constructor(val size: Ubyte, data: ByteArray) : TCPOption(3.ub, 3.ub, data) {
        constructor(data: ByteArray) : this(data.readUbyte(), data)
        constructor(size: Ubyte) : this(size, bufferOf(size))

        operator fun component1(): Ubyte = size

        override fun value(): String = "size=$size"
    }

    object SelectiveAckOK : TCPOption(4.ub, 2.ub)

    class SelectiveAck private constructor(val blocks: Array<Pair<Uint, Uint>>, data: ByteArray) : TCPOption(5.ub, (2 + blocks.size * 8).ub, data) {
        constructor(data: ByteArray) : this(data.toBlocks(), data)
        constructor(blocks: Array<Pair<Uint, Uint>>) : this(blocks, blocks.toByteArray())

        operator fun component1(): Array<Pair<Uint, Uint>> = blocks

        override fun value(): String = blocks.joinToString(prefix = "[", postfix = "]") { (left, right) -> "($left:$right)" }

        companion object {
            private fun Array<Pair<Uint, Uint>>.toByteArray(): ByteArray {
                val blocks = IntArray(size * 2)
                var i = 0
                for ((left, right) in this) {
                    blocks[i++] = left.toInt()
                    blocks[i++] = right.toInt()
                }
                return bufferOf(*blocks)
            }

            private fun ByteArray.toBlocks(): Array<Pair<Uint, Uint>> = Array(size / 8) { readUint(it * 2) to readUint(it * 2 + 1) }
        }
    }

    class Timestamp private constructor(val time: Uint, val echoReplayTime: Uint, data: ByteArray) : TCPOption(8.ub, 10.ub, data) {
        constructor(data: ByteArray) : this(data.readUint(), data.readUint(), data)
        constructor(time: Uint, echoReplayTime: Uint) : this(time, echoReplayTime, bufferOf(time, echoReplayTime))

        operator fun component1(): Uint = time
        operator fun component2(): Uint = echoReplayTime

        override fun value(): String = "time=$time, echoReplayTime=$echoReplayTime"

    }

    class AltChecksum private constructor(val algorithm: Byte, data: ByteArray) : TCPOption(14.ub, 3.ub, data) {
        constructor(data: ByteArray) : this(data.readByte(), data)
        constructor(algorithm: Byte) : this(algorithm, bufferOf(algorithm))

        operator fun component1(): Byte = algorithm

        override fun value(): String {
            if (algorithm < DisplayName.size)
                return "alg=${DisplayName[algorithm.toInt()]}"
            return "alg=$algorithm"
        }

        companion object {
            const val TCP_Checksum = 0
            const val Fletcher_8_Checksum = 1
            const val Fletcher_16_Checksum = 2
            const val Redundant_Checksum_Avoidance = 3
            private val DisplayName = arrayOf("TCP", "Fletcher 8bit", "Fletcher 16bit", "redundant")

        }
    }

    class AltChecksumOpt(val checksum: ByteArray) : TCPOption(15.ub, checksum.size.ub + 2, checksum) {
        operator fun component1(): ByteArray = checksum

        override fun value(): String = ToString.toHex(checksum)
    }

    class Mood(val mood: String, data: ByteArray) : TCPOption(25.ub, data.size.ub + 2, data) {
        constructor(data: ByteArray) : this(String(data, Charsets.US_ASCII), data)
        constructor(mood: String) : this(mood, mood.toByteArray(Charsets.US_ASCII))

        operator fun component1(): String = mood

        override fun value(): String = "mood=\"$mood\""
    }

    class Timeout private constructor(val granularity: Boolean, val size: Short, data: ByteArray) : TCPOption(28.ub, 4.ub, data) {
        constructor(data: ByteArray) : this((data[0] ushr 7) != 0.toByte(), (data.readUshort() shl 1).toShort(), data)
        constructor(granularity: Boolean, size: Short) : this(granularity, size, bufferOf((if (!granularity) size else size or (1.shr(15)).toUshort())))

        operator fun component1(): Boolean = granularity
        operator fun component2(): Short = size

        override fun value(): String = "g=${if (granularity) 1 else 0}, size=$size"
    }

    class Other(kind: Ubyte, length: Ubyte = 0.ub, data: ByteArray? = null) : TCPOption(kind, length, data) {
        operator fun component1(): Ubyte = kind
        operator fun component2(): Ubyte = length
        operator fun component3(): ByteArray? = data
    }
    //endregion

    override val marker get() = Companion

    companion object : IProtocolMarker<TCPOption>, KLogging() {
        override val name: String = "TCPOption"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is TCPOption
        override val defaultValue: TCPOption = NOP

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): TCPOption? = try {
            val kind = reader.readUbyte().toInt()
            when (kind) {
                0 -> EndOfOptions
                1 -> NOP
                else -> {
                    val length = reader.readUbyte().toInt()
                    val data = reader.readByteArray(length - 2)
                    generifyOf(Other(kind.toUbyte(), length.toUbyte(), data))
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a TCPOption packet." }
            null
        }

        fun generifyOf(option: Other): TCPOption {
            val kind = option.kind.toInt()
            val data = option.data ?: ByteArray(0)
            return when (kind) {
                0 -> EndOfOptions
                1 -> NOP
                2 -> MaxSegSize(data)
                3 -> ShiftCount(data)
                4 -> SelectiveAckOK
                5 -> SelectiveAck(data)
                8 -> Timestamp(data)
                14 -> AltChecksum(data)
                15 -> AltChecksumOpt(data)
                25 -> Mood(data)
                28 -> Timeout(data)
                else -> option
            }
        }

        private val KnownOptions: Map<Ubyte, String> = mapOf(
                0.ub to "EOL",
                1.ub to "NOP",
                2.ub to "MSS",
                3.ub to "WScale",
                4.ub to "SAckOK",
                5.ub to "SAck",
                8.ub to "Timestamp",
                14.ub to "AltChkSum",
                15.ub to "AltChkSumOpt",
                25.ub to "Mood",
                28.ub to "UTO",
                34.ub to "TFO"
        )

        private val ExtraKnownOptions: MutableMap<Ubyte, String> = mutableMapOf()

        fun addKnownOption(kind: Ubyte, name: String) {
            if (kind !in KnownOptions && kind !in ExtraKnownOptions) {
                ExtraKnownOptions[kind] = name
            }
        }
    }
}
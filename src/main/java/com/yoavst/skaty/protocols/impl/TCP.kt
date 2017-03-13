package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.model.*
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.protocols.impl.TCP.Option.Companion.format
import com.yoavst.skaty.utils.Struct
import com.yoavst.skaty.utils.ToString
import com.yoavst.skaty.utils.bufferOf
import unsigned.*

data class TCP(var sport: Ushort? = 20.us,
               var dport: Ushort? = 80.us,
               var seq: Uint? = 0.ui,
               var ack: Uint? = 0.ui,
               var dataofs: Boolean = false,
               var reserved: Boolean = false,
               var flags: Flags<Flag> = flagsOf(Flag.SYN),
               var window: Ushort = 8192.us,
               var chksum: Ushort? = null,
               var urgptr: Ushort? = 0.us,
               var options: Options = emptyOptions(),
               override var payload: IProtocol<*>? = null) : BaseProtocol<TCP>(), IP.Aware, Layer4 {
    override fun onPayload(ip: IP) {
        ip.proto = IP.Protocol.TCP
    }

    override fun toString(): String = ToString.generate(this)

    override fun clone(): TCP = copy()
    override val marker: IProtocolMarker<TCP> get() = Companion

    companion object : IProtocolMarker<TCP> {
        override val name: String get() = "TCP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is TCP
        override val defaultValue: TCP = TCP()

        fun optionsOf(vararg options: Option) = Options(options.toList())
        fun emptyOptions() = optionsOf()
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
        CWR(0x80)
    }
    //endregion

    //region Options
    data class Option(val kind: Ubyte, val length: Ubyte? = null, val data: ByteArray? = null) {
        override fun equals(other: Any?): Boolean {
            return other === this || (other is Option && other.kind == kind && other.length == length &&
                    data?.contentEquals(other.data ?: byteArrayOf()) ?: (other.data == null))
        }

        override fun hashCode(): Int {
            var result = kind.hashCode()
            result = 31 * result + (length?.hashCode() ?: 0)
            result = 31 * result + (data?.contentHashCode() ?: 0)
            return result
        }

        companion object : Formatter<Option> {
            val KnownEmptyOptions: MutableMap<Ubyte, String> = mutableMapOf(
                    0.ub to "EOL",
                    1.ub to "NOP"
            )
            val KnownOptionsWithData: MutableMap<Ubyte, Pair<String, String>> = mutableMapOf(
                    2.ub to ("MSS" to "H"),
                    3.ub to ("WScale" to "B"),
                    4.ub to ("SAckOK" to ""),
                    5.ub to ("SAck" to "P"),
                    8.ub to ("Timestamp" to "II"),
                    14.ub to ("AltChkSum" to "B"),
                    15.ub to ("AltChkSumOpt" to "P"),
                    25.ub to ("Mood" to "p"),
                    28.ub to ("UTO" to "H"),
                    34.ub to ("TFO" to "II")
            )

            override fun format(value: Option?): String {
                val kind = value?.kind ?: return ""
                return KnownEmptyOptions[kind] ?: KnownOptionsWithData[kind]?.let { (name, format) ->
                    "$name:${Struct.parse(format, value.data ?: ByteArray(0))}"
                } ?: "$value"
            }

            fun endOfOptions() = Option(0.ub)
            fun NOP() = Option(1.ub)
            fun maxSegSize(size: Ushort) = Option(2.ub, 4.ub, bufferOf(size))
            fun shiftCount(size: Ubyte) = Option(3.ub, 3.ub, byteArrayOf(size.toByte()))
            fun selectiveAckOK() = Option(4.ub, 2.ub)
            fun timestamp(time: Uint, echoReplayTime: Uint) = Option(8.ub, 8.ub, bufferOf(time, echoReplayTime))
            fun altChecksum(algorithm: Ubyte) = Option(14.ub, 3.ub, byteArrayOf(algorithm.toByte()))
            fun altChecksumOpt(checksum: ByteArray) = Option(15.ub, checksum.size.ub, checksum)
        }
    }

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
     */
    @Formatted(Options::class)
    class Options internal constructor(options: List<Option>) : BaseOptions<Options, Option>(), List<Option> by options {
        companion object : Formatter<Options> {
            override fun format(value: Options?): String {
                if (value == null) return "[]"
                return value.joinToString(prefix = "[", postfix = "]", transform = Option.Companion::format)
            }

        }
    }
    //endregion

}



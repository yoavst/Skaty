package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.field.*
import com.yoavst.skaty.model.BaseOptions
import com.yoavst.skaty.model.Flags
import com.yoavst.skaty.model.flagsOf
import com.yoavst.skaty.protocols.*
import unsigned.*
import kotlin.reflect.KProperty

class TCP constructor(sport: Ushort? = null,
                      dport: Ushort? = null,
                      seq: Uint? = null,
                      ack: Uint? = null,
                      dataofs: Boolean? = null,
                      reserved: Boolean? = null,
                      flags: Flags<Flag>? = null,
                      window: Ushort? = null,
                      chksum: Ushort? = null,
                      urgptr: Ushort? = null,
                      options: Options? = null,
                      override var payload: IProtocol<*>? = null) : IContainerProtocol<TCP>, IP.Aware, Layer4 {
    val sport: UShortField = UShortField("source port", 20.us).apply { setIf(sport) }
    val dport: UShortField = UShortField("destination port", 80.us).apply { setIf(dport) }
    val seq: UIntField = UIntField("sequence number", 0.ui).apply { setIf(seq) }
    val ack: UIntField = UIntField("acknowledge number", 0.ui).apply { setIf(ack) }
    val dataofs: BitField = BitField("dataofs", false).apply { setIf(dataofs) }
    val reserved: BitField = BitField("dataofs", false).apply { setIf(reserved) }
    val flags: FlagsField<Flag> = FlagsField("flags", flagsOf(Flag.SYN)).apply { setIf(flags) }
    val window: UShortField = UShortField("window size", 8192.us).apply { setIf(window) }
    val chksum: NullableUShortField = NullableUShortField("checksum", null).apply { setIf(chksum) }
    val urgptr: UShortField = UShortField("urgptr", 0.us).apply { setIf(urgptr) }
    val options: OptionsField<Options, Option> = OptionsField("tcp options", emptyOptions()).apply { setIf(options) }

    override fun onPayload(ip: IP) {
        ip.proto.value = IP.PROTOCOL_TCP
    }

    fun copy(sport: Ushort? = null, dport: Ushort? = null, seq: Uint? = null, ack: Uint? = null, dataofs: Boolean? = null,
             reserved: Boolean? = null, flags: Flags<Flag>? = null, window: Ushort? = null, chksum: Ushort? = null, urgptr: Ushort? = null,
             options: Options? = null, payload: IProtocol<*>? = null): TCP {
        return TCP(sport ?: this.sport(), dport ?: this.dport(), seq ?: this.seq(), ack ?: this.ack(),
                dataofs ?: this.dataofs(), reserved ?: this.reserved(), flags ?: this.flags(), window ?: this.window(),
                chksum ?: this.chksum(), urgptr ?: this.urgptr(), options ?: this.options(), payload ?: this.payload)
    }

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

        companion object {
            fun NOP() = Option(1.ub)
            fun endOfOptions() = Option(0.ub)
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
    class Options internal constructor(options: List<Option>) : BaseOptions<Options, Option>(), List<Option> by options

    //region Object methods
    override fun toString(): String {
        return "TCP(sport=${sport()}, dport=${dport()}, seq=${seq()}, ack=${ack()}, dataofs=${dataofs()}, reserved=${reserved()}, flags=${flags()}, window=${window()}, chksum=${chksum()}, urgptr=${urgptr()}, options=${options()}) -> $payload"
    }

    override fun clone(): TCP = copy()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TCP) return false

        if (payload != other.payload) return false
        if (sport != other.sport) return false
        if (dport != other.dport) return false
        if (seq != other.seq) return false
        if (ack != other.ack) return false
        if (dataofs != other.dataofs) return false
        if (reserved != other.reserved) return false
        if (flags != other.flags) return false
        if (window != other.window) return false
        if (chksum != other.chksum) return false
        if (urgptr != other.urgptr) return false
        if (options != other.options) return false
        return true
    }

    override fun hashCode(): Int {
        var result = payload?.hashCode() ?: 0
        result = 31 * result + sport.hashCode()
        result = 31 * result + dport.hashCode()
        result = 31 * result + seq.hashCode()
        result = 31 * result + ack.hashCode()
        result = 31 * result + dataofs.hashCode()
        result = 31 * result + reserved.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + window.hashCode()
        result = 31 * result + chksum.hashCode()
        result = 31 * result + urgptr.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    override val marker: IProtocolMarker<TCP> get() = Companion

    //endregion
    companion object : IProtocolMarker<TCP> {
        override val name: String get() = "TCP"
        override val fields: Set<KProperty<Field<*>>> = setOf(
                TCP::sport, TCP::dport, TCP::seq, TCP::ack, TCP::dataofs, TCP::reserved,
                TCP::flags, TCP::window, TCP::chksum, TCP::urgptr, TCP::options
        )

        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is TCP

        fun optionsOf(vararg options: Option) = Options(options.toList())
        fun emptyOptions() = optionsOf()
    }
}



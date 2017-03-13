package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.model.*
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.utils.ToString
import unsigned.*

data class IP(var version: Ubyte = 4.ub,
              var ihl: Ubyte? = null,
              var tos: Ubyte = 0.ub,
              var ecn: ECN = ECN.NonECT,
              var len: Ushort? = null,
              var id: Ushort = 1.us,
              var flags: Flags<Flag>? = emptyFlags(),
              var ttl: Ubyte = 64.ub,
              @property:Formatted(Protocol::class) var proto: Ubyte? = 0.ub,
              var chksum: Ushort? = null,
              var src: Address? = null,
              var dst: Address = ip("127.0.0.1"),
              var options: Options = emptyOptions(),
              @property:Exclude private var _payload: IProtocol<*>? = null) : BaseProtocol<IP>(), Ether.Aware, Layer3 {

    override var payload: IProtocol<*>?
        get() = _payload
        set(value) {
            _payload = value
            (value as? Aware)?.onPayload(this)
        }

    override fun onPayload(ether: Ether) {
        ether.type = Ether.Type.IP
    }

    override fun toString(): String = ToString.generate(this)
    override fun clone(): IP = copy()
    override val marker: IProtocolMarker<IP> get() = IP

    companion object : IProtocolMarker<IP> {
        override val name: String get() = "IP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is IP
        override val defaultValue: IP = IP()

        fun optionsOf(vararg options: Option) = Options(options.toList())
        fun emptyOptions() = optionsOf()
    }

    interface Aware {
        fun onPayload(ip: IP)
    }

    //region Data objects
    enum class Flag(value: Int) {
        Reserved(0x0),
        DF(0x1),
        MF(0x2)
    }

    enum class ECN(value: Int) {
        NonECT(0),
        ECT1(1),
        ECT0(2),
        CE(3)
    }

    data class Address(val raw: Uint) {
        override fun toString(): String = raw.toFormattedIpAddress()
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

    //region Options
    data class Option(val copied: Boolean, val clazz: Class, val number: Byte, val length: Ubyte, val data: ByteArray? = null) {
        override fun equals(other: Any?): Boolean {
            return other === this || (other is Option && other.copied == copied && other.clazz == clazz &&
                    other.number == number && other.length == length && data?.contentEquals(other.data ?: byteArrayOf()) ?: (other.data == null))
        }

        override fun hashCode(): Int {
            var result = copied.hashCode()
            result = 31 * result + clazz.hashCode()
            result = 31 * result + number.hashCode()
            result = 31 * result + length.hashCode()
            result = 31 * result + (data?.contentHashCode() ?: 0)
            return result
        }
    }

    enum class Class(val value: Int) {
        Control(0),
        Reserved1(1),
        Debugging(2),
        Reserved3(3)
    }

    class Options internal constructor(options: List<Option>) : BaseOptions<Options, Option>(), List<Option> by options
    //endregion


}

package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.field.*
import com.yoavst.skaty.model.BaseOptions
import com.yoavst.skaty.model.Flags
import com.yoavst.skaty.model.emptyFlags
import com.yoavst.skaty.protocols.*
import unsigned.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class IP(version: Ubyte? = null,
         ihl: Ubyte? = null,
         tos: Ubyte? = null,
         ecn: ECN? = null,
         len: Ushort? = null,
         id: Ushort? = null,
         flags: Flags<Flag>? = null,
         ttl: Ubyte? = null,
         proto: Ubyte? = null,
         chksum: Ushort? = null,
         src: Address? = null,
         dst: Address? = null,
         options: Options? = null,
         payload: IProtocol<*>? = null) : IContainerProtocol<IP>, Ether.Aware, Layer3 {
    override var payload: IProtocol<*>? by Delegates.observable(payload) { _, _, new ->
        (new as? Aware)?.onPayload(this)
    }

    override fun onPayload(ether: Ether) {
        ether.type.value = Ether.TYPE_IP
    }

    val version: UByteField = UByteField("version", 4.ub).apply { setIf(version) }
    val ihl: NullableUByteField = NullableUByteField("internet header length", null).apply { setIf(ihl) }
    val tos: UByteField = UByteField("type of service", 0.ub).apply { setIf(tos) }
    val ecn: EnumField<ECN> = EnumField("Explicit Congestion Notification", ECN.NonECT).apply { setIf(ecn) }
    val len: NullableUShortField = NullableUShortField("total length", null).apply { setIf(len) }
    val id: UShortField = UShortField("source port", 1.us).apply { setIf(id) }
    val flags: FlagsField<Flag> = FlagsField("IP flags", emptyFlags<Flag>()).apply { setIf(flags) }
    val ttl: UByteField = UByteField("time to live", 64.ub).apply { setIf(ttl) }
    val proto: UByteField = UByteField("protocol", 0.ub).apply { setIf(proto) }
    val chksum: NullableUShortField = NullableUShortField("checksum", null).apply { setIf(chksum) }
    val src: NullableIPAddressField = NullableIPAddressField("source address", null).apply { setIf(src) }
    val dst: IPAddressField = IPAddressField("destination address", ip("127.0.0.1")).apply { setIf(dst) }
    val options: OptionsField<Options, Option> = OptionsField("options", emptyOptions()).apply { setIf(options) }

    fun copy(version: Ubyte? = null, ihl: Ubyte? = null, tos: Ubyte? = null, ecn: ECN? = null, len: Ushort? = null, id: Ushort? = null,
             flags: Flags<Flag>? = null, ttl: Ubyte? = null, proto: Ubyte? = null, chksum: Ushort? = null, src: Address? = null,
             dst: Address? = null, options: Options? = null, payload: IProtocol<*>? = null): IP {
        return IP(version ?: this.version(), ihl ?: this.ihl(), tos ?: this.tos(), ecn ?: this.ecn(),
                len ?: this.len(), id ?: this.id(), flags ?: this.flags(), ttl ?: this.ttl(),
                proto ?: this.proto(), chksum ?: this.chksum(), src ?: this.src(), dst ?: this.dst(),
                options ?: this.options(), payload ?: this.payload)
    }

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

    interface Aware {
        fun onPayload(ip: IP)
    }


    //region Object methods
    override fun toString(): String {
        return "IP(version=$version, ihl=$ihl, tos=$tos, ecn=$ecn, len=$len, id=$id, flags=$flags, ttl=$ttl, proto=$proto, chksum=$chksum, src=$src, dst=$dst, options=$options) -> $payload"
    }

    override fun clone(): IP = copy()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IP) return false

        if (payload != other.payload) return false
        if (version != other.version) return false
        if (ihl != other.ihl) return false
        if (tos != other.tos) return false
        if (ecn != other.ecn) return false
        if (len != other.len) return false
        if (id != other.id) return false
        if (flags != other.flags) return false
        if (ttl != other.ttl) return false
        if (proto != other.proto) return false
        if (chksum != other.chksum) return false
        if (src != other.src) return false
        if (dst != other.dst) return false
        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload?.hashCode() ?: 0
        result = 31 * result + version.hashCode()
        result = 31 * result + ihl.hashCode()
        result = 31 * result + tos.hashCode()
        result = 31 * result + ecn.hashCode()
        result = 31 * result + len.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + proto.hashCode()
        result = 31 * result + chksum.hashCode()
        result = 31 * result + src.hashCode()
        result = 31 * result + dst.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    override val marker: IProtocolMarker<IP> get() = IP

    //endregion
    companion object : IProtocolMarker<IP> {
        override val name: String get() = "IP"
        override val fields: Set<KProperty<Field<*>>> = setOf(
                IP::version, IP::ihl, IP::tos, IP::ecn, IP::len, IP::id, IP::flags, IP::ttl, IP::proto, IP::chksum, IP::src, IP::dst, IP::options
        )
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is IP

        fun optionsOf(vararg options: Option) = Options(options.toList())
        fun emptyOptions() = optionsOf()

        val PROTOCOL_TCP = 6.ub
        val PROTOCOL_UDP = 17.ub
    }
}

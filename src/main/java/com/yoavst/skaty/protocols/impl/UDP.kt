package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.field.Field
import com.yoavst.skaty.field.NullableUShortField
import com.yoavst.skaty.field.UShortField
import com.yoavst.skaty.protocols.IContainerProtocol
import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IProtocolMarker
import com.yoavst.skaty.protocols.Layer4
import unsigned.Ushort
import unsigned.us
import kotlin.reflect.KProperty

class UDP(
        sport: Ushort? = null,
        dport: Ushort? = null,
        len: Ushort? = null,
        chksum: Ushort? = null,
        override var payload: IProtocol<*>? = null
) : IContainerProtocol<UDP>, IP.Aware, Layer4 {
    val sport: UShortField = UShortField("source port", 53.us).apply { setIf(sport) }
    val dport: UShortField = UShortField("destination port", 53.us).apply { setIf(dport) }
    val len: NullableUShortField = NullableUShortField("length", null).apply { setIf(len) }
    val chksum: NullableUShortField = NullableUShortField("Checksum", null).apply { setIf(chksum) }

    override fun onPayload(ip: IP) {
        ip.proto.value = IP.PROTOCOL_UDP
    }

    fun copy(sport: Ushort? = null, dport: Ushort? = null, len: Ushort? = null, chksum: Ushort? = null, payload: IProtocol<*>? = null): UDP {
        return UDP(sport ?: this.sport(), dport ?: this.dport(), len ?: this.len(), chksum ?: this.chksum(), payload ?: this.payload)
    }

    //region Object methods
    override fun toString(): String = "UDP(payload=$payload, sport=$sport, dport=$dport, len=$len, chksum=$chksum) -> $payload"

    override fun clone(): UDP = copy()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UDP) return false
        if (payload != other.payload) return false
        if (sport != other.sport) return false
        if (dport != other.dport) return false
        if (len != other.len) return false
        if (chksum != other.chksum) return false
        return true
    }
    override fun hashCode(): Int {
        var result = payload?.hashCode() ?: 0
        result = 31 * result + sport.hashCode()
        result = 31 * result + dport.hashCode()
        result = 31 * result + len.hashCode()
        result = 31 * result + chksum.hashCode()
        return result
    }
    override val marker: IProtocolMarker<UDP> get() = UDP
    //endregion

    companion object : IProtocolMarker<UDP> {
        override val name: String get() = "UDP"
        override val fields: Set<KProperty<Field<*>>> = setOf(UDP::sport, UDP::dport, UDP::len, UDP::chksum)
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is UDP
    }
}
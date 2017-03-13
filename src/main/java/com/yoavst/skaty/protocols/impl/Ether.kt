package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.field.Field
import com.yoavst.skaty.field.NullableMACAddressField
import com.yoavst.skaty.field.UShortField
import com.yoavst.skaty.protocols.*
import unsigned.Ulong
import unsigned.Ushort
import unsigned.us
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class Ether(
        dst: MAC? = null,
        src: MAC? = null,
        type: Ushort? = null,
        payload: IProtocol<*>? = null) : IContainerProtocol<Ether>, Layer2 {
    override var payload: IProtocol<*>? by Delegates.observable(payload) { _, _, new ->
        (new as? Aware)?.onPayload(this)
    }

    val dst: NullableMACAddressField = NullableMACAddressField("destination address", null).apply { setIf(dst) }
    val src: NullableMACAddressField = NullableMACAddressField("source address", null).apply { setIf(src) }
    val type: UShortField = UShortField("type", 0.us).apply { setIf(type) }

    fun copy(dst: MAC? = null, src: MAC? = null, type: Ushort? = null, payload: IProtocol<*>? = null): Ether {
        return Ether(dst ?: this.dst(), src ?: this.src(), type ?: this.type(), payload ?: this.payload)
    }

    data class MAC(val raw: Ulong) {
        override fun toString(): String = raw.toFormattedMacAddress()
    }

    interface Aware {
        fun onPayload(ether: Ether)
    }

    //region Object methods
    override fun toString(): String {
        return "Ether(dst=$dst, src=$src, type=$type) -> $payload"
    }

    override fun clone(): Ether = copy()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ether) return false

        if (payload != other.payload) return false
        if (dst != other.dst) return false
        if (src != other.src) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload?.hashCode() ?: 0
        result = 31 * result + dst.hashCode()
        result = 31 * result + src.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override val marker: IProtocolMarker<Ether> get() = Ether

    //endregion
    companion object : IProtocolMarker<Ether> {
        override val name: String get() = "Ethernet"
        override val fields: Set<KProperty<Field<*>>> = setOf(Ether::dst, Ether::src, Ether::type)

        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Ether

        val TYPE_IP = 0x0800.us
    }
}


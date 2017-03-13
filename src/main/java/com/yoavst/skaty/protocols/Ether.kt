package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.Formatter
import com.yoavst.skaty.protocols.interfaces.IProtocol
import com.yoavst.skaty.protocols.interfaces.IProtocolMarker
import com.yoavst.skaty.utils.ToString
import unsigned.*

data class Ether(
        var dst: MAC? = null,
        var src: MAC? = null,
        @property:Formatted(Type::class) var type: Ushort = 0.us,
        @property:Exclude private var _payload: IProtocol<*>? = null) : BaseProtocol<Ether>(), Layer2 {
    override var payload: IProtocol<*>?
        get() = _payload
        set(value) {
            _payload = value
            (value as? Aware)?.onPayload(this)
        }

    override fun toString(): String = ToString.generate(this)
    override fun clone(): Ether = copy()
    override val marker: IProtocolMarker<Ether> get() = Companion

    companion object : IProtocolMarker<Ether> {
        override val name: String get() = "Ethernet"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Ether
        override val defaultValue: Ether = Ether()
    }

    interface Aware {
        fun onPayload(ether: Ether)
    }

    //region Data objects
    data class MAC(val raw: Ulong) {
        override fun toString(): String = raw.toFormattedMacAddress()
    }

    object Type: Formatter<Ushort> {
        val IP = 0x0800.us

        var KnownFormats: MutableMap<Ushort, String> = mutableMapOf(
                IP to "IP"
        )

        override fun format(value: Ushort?): String = KnownFormats.getOrDefault(value ?: 0.us, "$value")
    }
    //endregion
}


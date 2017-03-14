package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.UshortHexFormatter
import com.yoavst.skaty.protocols.interfaces.IProtocol
import com.yoavst.skaty.protocols.interfaces.IProtocolMarker
import com.yoavst.skaty.utils.ToString
import unsigned.Ushort
import unsigned.us

data class UDP(var sport: Ushort = 53.us,
               var dport: Ushort = 53.us,
               var len: Ushort? = null,
               @property:Formatted(UshortHexFormatter::class) var chksum: Ushort? = null,
               override var payload: IProtocol<*>? = null) : BaseProtocol<UDP>(), IP.Aware, Layer4 {
    override fun onPayload(ip: IP) {
        ip.proto = IP.Protocol.UDP
    }

    override fun toString(): String = ToString.generate(this)
    override fun clone(): UDP = copy()
    override val marker: IProtocolMarker<UDP> get() = Companion

    companion object : IProtocolMarker<UDP> {
        override val name: String get() = "UDP"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is UDP
        override val defaultValue: UDP = UDP()
    }
}
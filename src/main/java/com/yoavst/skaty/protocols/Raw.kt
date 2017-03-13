package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.BaseProtocol
import com.yoavst.skaty.protocols.interfaces.IProtocol
import com.yoavst.skaty.protocols.interfaces.IProtocolMarker
import com.yoavst.skaty.utils.ToString

data class Raw(var load: String = "",
               override var payload: IProtocol<*>? = null) : BaseProtocol<Raw>() {

    override fun toString(): String = ToString.generate(this)
    override fun clone(): Raw = copy()
    override val marker: IProtocolMarker<Raw> get() = Companion

    companion object : IProtocolMarker<Raw> {
        override val name: String get() = "Raw"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Raw
        override val defaultValue: Raw = Raw()
    }
}
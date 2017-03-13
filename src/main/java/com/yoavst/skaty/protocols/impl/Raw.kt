package com.yoavst.skaty.protocols.impl

import com.yoavst.skaty.field.Field
import com.yoavst.skaty.field.StringField
import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IProtocolMarker
import kotlin.reflect.KProperty

class Raw(load: String) : IProtocol<Raw> {
    val load: StringField = StringField("load", "").apply { setIf(load) }

    fun copy(load: String? = null) = Raw(load ?: this.load())

    //region Object methods
    override fun toString(): String = "\"$load\""

    override fun clone(): Raw = copy()
    override fun equals(other: Any?): Boolean = other === this || (other is Raw && other.load() == load())
    override fun hashCode(): Int = load().hashCode()
    override val marker: IProtocolMarker<Raw> get() = Raw
    //endregion

    companion object : IProtocolMarker<Raw> {
        override val name: String get() = "Raw"
        override val fields: Set<KProperty<Field<*>>> = setOf(Raw::load)
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Raw
    }
}
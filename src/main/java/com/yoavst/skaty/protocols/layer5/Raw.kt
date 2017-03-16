@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.declarations.IProtocol
import com.yoavst.skaty.serialization.SerializationContext
import com.yoavst.skaty.serialization.SimpleReader
import com.yoavst.skaty.serialization.readAsByteArray
import com.yoavst.skaty.utils.ToString

data class Raw(var load: String = "") : IProtocol<Raw> {
    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion

    companion object : IProtocolMarker<Raw> {
        override val name: String get() = "Raw"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Raw
        override val defaultValue: Raw = Raw()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): Raw? = Raw(String(reader.readAsByteArray(), Charsets.US_ASCII))
    }
}
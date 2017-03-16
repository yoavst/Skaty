@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.Formatter
import com.yoavst.skaty.protocols.declarations.IProtocol
import com.yoavst.skaty.protocols.declarations.IProtocolMarker
import com.yoavst.skaty.protocols.declarations.Layer2
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.utils.ToString
import mu.KLogging
import unsigned.Ulong
import unsigned.Ushort
import unsigned.ub
import unsigned.us

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
    override val marker get() = Companion

    companion object : IProtocolMarker<Ether>, KLogging() {
        override val name: String get() = "Ethernet"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Ether
        override val defaultValue: Ether = Ether()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): Ether? = try {
//            // including Preamble and SFD
//            reader.readByteArray(7) // preamble
//            require(reader.readUbyte() == 0xAB.ub) { "Not an ethernet packet" } // SFD

            val dst = mac(reader.readByteArray(6))
            val src = mac(reader.readByteArray(6))

            val etherType = reader.readUshort()

            val ether = Ether(dst, src, etherType)
            ether._payload = serializationContext.serialize(reader, ether)

            ether
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse the packet to a TCP packet." }
            null
        }
    }

    fun isSize(): Boolean = type <= 1500
    fun isType(): Boolean = type >= 1536

    interface Aware {
        fun onPayload(ether: Ether)
    }

    //region Data objects
    data class MAC(val raw: Ulong) {
        override fun toString(): String = raw.toFormattedMacAddress()

        fun toByteArray() = bufferOf(raw)
    }

    object Type : Formatter<Ushort> {
        val IP = 0x0800.us

        var KnownFormats: MutableMap<Ushort, String> = mutableMapOf(
                IP to "IP"
        )

        override fun format(value: Ushort?): String = KnownFormats.getOrDefault(value ?: 0.us, "$value")
    }
    //endregion
}


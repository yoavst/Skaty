@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.Formatter
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString

data class Raw(@property:Formatted(Companion::class) var load: ByteArray = byteArrayOf(), override var parent: IProtocol<*>? = null) : IProtocol<Raw> {
    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion
    override fun headerSize(): Int = 0

    operator fun contains(text: String) = text in String(load)

    override fun write(writer: SimpleWriter, stage: SerializationContext.Stage) {
        when (stage) {
            Stage.Data -> {
                writer.writeByteArray(load)
            }
            Stage.Length -> {
                writer.skip(load.size)
            }
            Stage.Checksum -> {
                writer.index -= load.size
            }
        }

    }

    override fun equals(other: Any?): Boolean = (other as? Raw)?.load?.contentEquals(load) ?: false

    override fun hashCode(): Int = load.contentHashCode()

    companion object : IProtocolMarker<Raw>, Formatter<ByteArray> {
        override val name: String get() = "Raw"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is Raw
        override val defaultValue: Raw = Raw()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): Raw? = Raw(reader.readAsByteArray())

        override fun format(value: ByteArray?): String = "\"${(value?.let { String(it, Charsets.UTF_8) } ?: "").replace("\r\n", "\\n").replace("\n", "\\n")}\""
    }
}
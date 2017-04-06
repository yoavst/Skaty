package com.yoavst.skaty

import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IProtocolMarker
import com.yoavst.skaty.serialization.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
import com.yoavst.skaty.utils.ToString
import unsigned.Ubyte
import unsigned.ub

class TestProtocol(var usernameLength: Ubyte = 0.ub, var username: String = "",
                   var command: Byte = 0, var parameters: ByteArray = ByteArray(0),
                   override var parent: IProtocol<*>? = null) : IProtocol<TestProtocol> {

    override fun write(writer: SimpleWriter, stage: Stage) {
        when (stage) {
            Stage.Data -> {
                val bytes = username.toByteArray(charset = Charsets.UTF_8)
                if (usernameLength == 0.ub)
                    usernameLength = bytes.size.ub
                writer.writeUbyte(usernameLength)
                writer.writeByteArray(bytes, length = usernameLength.toInt())
                writer.writeByte(command)
                writer.writeByteArray(parameters)
            }
            Stage.Length -> {
                writer.skip(headerSize())

            }
            Stage.Checksum -> {
                writer.index -= headerSize()
            }
        }

    }

    override fun toString(): String = ToString.generate(this)
    override val marker get() = Companion
    override fun headerSize(): Int = 2 + username.toByteArray().size + parameters.size

    /* If protocol usage ByteArray, it has to reimplement equals(any) and hashcode() functions */

    override fun equals(other: Any?): Boolean {
        return other === this || (other is TestProtocol && other.usernameLength == usernameLength && other.username == username &&
                other.command == command && parameters.contentEquals(other.parameters ?: byteArrayOf()))
    }

    override fun hashCode(): Int {
        var result = usernameLength.toInt()
        result = 31 * result + username.hashCode()
        result = 31 * result + command
        result = 31 * result + parameters.contentHashCode()
        return result
    }

    companion object : IProtocolMarker<TestProtocol> {
        override val name: String get() = "12.6 Chat"
        override fun isProtocol(protocol: IProtocol<*>): Boolean = protocol is TestProtocol
        override val defaultValue: TestProtocol = TestProtocol()

        override fun of(reader: SimpleReader, serializationContext: SerializationContext): TestProtocol? = try {
            val usernameLength = reader.readUbyte()
            val username = String(reader.readByteArray(usernameLength.toInt()), charset = Charsets.UTF_8)
            val command = reader.readByte()
            val parameters = reader.readAsByteArray() // read until the end
            TestProtocol(usernameLength, username, command, parameters)
        } catch (e: Exception) {
            println("Failed to serialize to 12.6 chat protocol")
            e.printStackTrace()
            null
        }
    }
}
package com.yoavst.skaty.serialization

import java.io.Closeable
import java.nio.ByteBuffer

class ByteArraySimpleWriter(private val buffer: ByteBuffer, private val closing: Closeable? = null) : SimpleWriter {
    constructor(array: ByteArray) : this(ByteBuffer.wrap(array))

    override val size: Int get() = buffer.capacity()
    override var index: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
        }

    override fun writeBool(value: Boolean) {
        buffer.put(if (value) 0.toByte() else 1.toByte())
    }

    override fun writeByte(value: Byte) {
        buffer.put(value)
    }

    override fun writeShort(value: Short) {
        buffer.putShort(value)
    }

    override fun writeInt(value: Int) {
        buffer.putInt(value)
    }

    override fun writeLong(value: Long) {
        buffer.putLong(value)
    }

    override fun writeByteArray(value: ByteArray, offset: Int, length: Int) {
        if (length == -1)
            buffer.put(value)
        else
            buffer.put(value, offset, length)
    }

    override fun close() = closing?.close() ?: Unit

    override fun array(): ByteArray = buffer.array().clone()
}

class ByteArraySimpleReader(private val array: ByteArray) : SimpleReader {
    var index = 0

    override fun readBool(): Boolean = array.readByte(index(1)) != 0.toByte()
    override fun readByte(): Byte = array.readByte(index(1))
    override fun readShort(): Short = array.readShort(index(2))
    override fun readInt(): Int = array.readInt(index(4))
    override fun readLong(): Long = array.readLong(index(8))

    override fun readByteArray(length: Int): ByteArray {
        if (length == 0)
            return ByteArray(0)

        if (length == -1)
            return ByteArray(array.size - index) { array.readByte(index(1)) }

        return ByteArray(length) { array.readByte(index(1) + it) }
    }

    override fun close() = Unit

    override fun skip(bytes: Int): Int {
        val max = array.size - index - 1
        if (max > bytes) {
            index += bytes
            return bytes
        } else {
            index = array.size - 1
            return max
        }
    }

    override fun hasMore(): Boolean = index < array.size

    private fun index(inc: Int): Int {
        val current = index
        index += inc
        return current
    }

}

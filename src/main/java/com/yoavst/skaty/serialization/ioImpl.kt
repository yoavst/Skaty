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

class ByteArraySimpleReader(private val buffer: ByteBuffer) : SimpleReader {
    constructor(array: ByteArray) : this(ByteBuffer.wrap(array))

    override fun readBool(): Boolean = buffer.get().toInt() != 0
    override fun readByte(): Byte = buffer.get()
    override fun readShort(): Short = buffer.short
    override fun readInt(): Int = buffer.int
    override fun readLong(): Long = buffer.long

    override fun readByteArray(length: Int): ByteArray = ByteArray(length).apply { buffer.get(this) }

    override fun close() = Unit

    override fun skip(bytes: Int) = buffer.position().let { buffer.position(it + bytes).position() - it }
}

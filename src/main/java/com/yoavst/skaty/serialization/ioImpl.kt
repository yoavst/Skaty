package com.yoavst.skaty.serialization

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteArraySimpleWriter(private val buffer: ByteBuffer, private val closing: Closeable? = null) : SimpleWriter {
    constructor(array: ByteArray) : this(ByteBuffer.wrap(array))

    override val size: Int get() = buffer.capacity()
    override var index: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
            updateMax()
        }

    override var maxIndex: Int = 0
        private set

    override fun writeBool(value: Boolean) {
        buffer.put(if (value) 0.toByte() else 1.toByte())
        updateMax()
    }

    override fun writeByte(value: Byte) {
        buffer.put(value)
        updateMax()
    }

    override fun writeShort(value: Short) {
        buffer.putShort(value)
        updateMax()
    }

    override fun writeInt(value: Int) {
        buffer.putInt(value)
        updateMax()
    }

    override fun writeLong(value: Long) {
        buffer.putLong(value)
        updateMax()
    }

    override fun writeByteArray(value: ByteArray, offset: Int, length: Int) {
        if (value.isNotEmpty()) {
            if (length == -1) {
                buffer.put(value)
                updateMax()
            } else {
                buffer.put(value, offset, length)
                updateMax()
            }
        }
    }

    override fun close() = closing?.close() ?: Unit

    override fun array(): ByteArray = buffer.array().clone()

    private fun updateMax() {
        if (buffer.position() > maxIndex)
            maxIndex = buffer.position()
    }
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

        if (length < 0) {
            if (array.size - index <= 0)
                return ByteArray(0)
            return ByteArray(array.size - index) { array.readByte(index(1)) }
        }

        return ByteArray(length) { array.readByte(index(1)) }
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

class BufferSimpleReader(private val buffer: ByteBuffer) : EndianSimpleReader {
    constructor(array: ByteArray) : this(ByteBuffer.wrap(array))

    override fun readBool(): Boolean = buffer.get().toInt() != 0
    override fun readByte(): Byte = buffer.get()
    override fun readShort(): Short = buffer.short
    override fun readInt(): Int = buffer.int
    override fun readLong(): Long = buffer.long

    override fun readByteArray(length: Int): ByteArray {
        if (length == -1) {
            return ByteArray(buffer.remaining()).apply { buffer.get(this) }
        } else
            return ByteArray(length).apply { buffer.get(this) }

    }

    override fun close() = Unit

    override fun skip(bytes: Int) = buffer.position().let { buffer.position(it + bytes).position() - it }

    override fun hasMore(): Boolean = buffer.hasRemaining()

    override fun bigEndian() {
        buffer.order(ByteOrder.BIG_ENDIAN)
    }

    override fun littleEndian() {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }
}


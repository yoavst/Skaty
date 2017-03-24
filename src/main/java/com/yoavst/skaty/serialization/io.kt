package com.yoavst.skaty.serialization

import unsigned.*
import java.io.Closeable
import java.nio.charset.Charset

interface SimpleWriter : Closeable {
    val size: Int
    var index: Int
    val maxIndex: Int

    fun writeBool(value: Boolean)
    fun writeByte(value: Byte)
    fun writeShort(value: Short)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeByteArray(value: ByteArray, offset: Int = 0, length: Int = -1)

    fun array(): ByteArray
}

fun SimpleWriter.writeUbyte(value: Ubyte) = writeByte(value.toByte())
fun SimpleWriter.writeUshort(value: Ushort) = writeShort(value.toShort())
fun SimpleWriter.writeUint(value: Uint) = writeInt(value.toInt())
fun SimpleWriter.writeUlong(value: Ulong) = writeLong(value.toLong())
fun SimpleWriter.writeString(value: String, charset: Charset = Charsets.UTF_8) = writeByteArray(value.toByteArray(charset))
fun SimpleWriter.skip(bytes: Int): Int {
    if (bytes > 0) {
        if (bytes + index >= size) {
            val d = size - index - 1
            index = size - 1
            return d
        }

        index += bytes
        return bytes
    } else return 0
}

interface SimpleReader : Closeable {
    fun readBool(): Boolean
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    /**
     * if [length] == -1, read all the buffer
     */
    fun readByteArray(length: Int): ByteArray

    fun skip(bytes: Int): Int
    fun hasMore(): Boolean
}

interface EndianSimpleReader : SimpleReader {
    fun bigEndian()
    fun littleEndian()
}

fun SimpleReader.readUbyte() = readByte().ub
fun SimpleReader.readUshort() = readShort().us
fun SimpleReader.readUint() = readInt().ui
fun SimpleReader.readUlong() = readLong().ul
fun SimpleReader.readString(length: Int, charset: Charset): String = String(readByteArray(length), charset)
fun SimpleReader.readAsByteArray(): ByteArray = readByteArray(-1)

package com.yoavst.skaty.serialization

import unsigned.*
import java.io.Closeable
import java.nio.charset.Charset

interface SimpleWriter : Closeable {
    val size: Int
    var index: Int

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
fun SimpleWriter.writeString(value: String, charset: Charset) = writeByteArray(value.toByteArray(charset))

interface SimpleReader : Closeable {
    fun readBool(): Boolean
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readByteArray(length: Int): ByteArray
    fun skip(bytes: Int): Int
}

fun SimpleReader.readUbyte() = readByte().ub
fun SimpleReader.readUshort() = readShort().us
fun SimpleReader.readUint() = readInt().ui
fun SimpleReader.readUlong() = readLong().ul
fun SimpleReader.readString(length: Int, charset: Charset): String = String(readByteArray(length), charset)

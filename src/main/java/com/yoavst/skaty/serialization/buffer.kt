package com.yoavst.skaty.serialization

import unsigned.Ubyte
import unsigned.Uint
import unsigned.Ulong
import unsigned.Ushort
import java.nio.ByteBuffer

fun bufferOf(vararg bytes: Byte): ByteArray = bytes

fun bufferOf(vararg bytes: Ubyte): ByteArray {
    return ByteArray(bytes.size) { bytes[it].toByte() }
}

fun bufferOf(vararg ints: Int): ByteArray {
    val buffer = ByteBuffer.allocate(ints.size * 4)
    ints.forEach { buffer.putInt(it) }
    return buffer.array()
}

fun bufferOf(vararg ints: Uint): ByteArray {
    val buffer = ByteBuffer.allocate(ints.size * 4)
    ints.forEach { buffer.putInt(it.toInt()) }
    return buffer.array()
}

fun bufferOf(vararg shorts: Short): ByteArray {
    val buffer = ByteBuffer.allocate(shorts.size * 2)
    shorts.forEach { buffer.putShort(it) }
    return buffer.array()
}

fun bufferOf(vararg shorts: Ushort): ByteArray {
    val buffer = ByteBuffer.allocate(shorts.size * 2)
    shorts.forEach { buffer.putShort(it.toShort()) }
    return buffer.array()
}

fun bufferOf(vararg longs: Ulong): ByteArray {
    val buffer = ByteBuffer.allocate(longs.size * 8)
    longs.forEach { buffer.putLong(it.toLong()) }
    return buffer.array()
}

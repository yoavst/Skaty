package com.yoavst.skaty.serialization

import unsigned.Uint
import unsigned.Ushort
import java.nio.ByteBuffer

fun bufferOf(vararg ints: Uint): ByteArray {
    val buffer = ByteBuffer.allocate(ints.size * 4)
    ints.forEach { buffer.putInt(it.toInt()) }
    return buffer.array()
}

fun bufferOf(vararg shorts: Ushort): ByteArray {
    val buffer = ByteBuffer.allocate(shorts.size * 2)
    shorts.forEach { buffer.putShort(it.toShort()) }
    return buffer.array()
}
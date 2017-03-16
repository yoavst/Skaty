package com.yoavst.skaty.serialization

import unsigned.*

fun ByteArray.readChar(index: Int = 0): Char = this[index].toChar()
fun ByteArray.readByte(index: Int = 0): Byte = this[index]
fun ByteArray.readUbyte(index: Int = 0): Ubyte = this[index].toUbyte()
fun ByteArray.readShort(index: Int = 0): Short = ((this[index].toUInt() shl 8) or this[index + 1].toUInt()).toShort()
fun ByteArray.readUshort(index: Int = 0): Ushort = ((this[index].toUInt() shl 8) or this[index + 1].toUInt()).us
fun ByteArray.readInt(index: Int = 0): Int = ((this[index].toUInt() shl 24) or (this[index + 1].toUInt() shl 16) or (this[index + 2].toUInt() shl 8) or this[index + 3].ui)
fun ByteArray.readUint(index: Int = 0): Uint = ((this[index].toUInt() shl 24) or (this[index + 1].toUInt() shl 16) or (this[index + 2].toUInt() shl 8) or this[index + 3].ui).ui
fun ByteArray.readLong(index: Int = 0): Long {
    return ((this[index].toULong() shl 56) + (this[index + 1].toULong() shl 48) + (this[index + 2].toULong() shl 40) + (this[index + 3].toULong().ui shl 32) +
            (this[index + 4].toULong() shl 24) + (this[index + 5].toULong() shl 16) + (this[index + 6].toULong() shl 8) + (this[index + 7].toULong().ui))
}

fun ByteArray.readUlong(index: Int = 0): Ulong {
    return ((this[index].toULong() shl 56) + (this[index + 1].toULong() shl 48) + (this[index + 2].toULong() shl 40) + (this[index + 3].toULong().ui shl 32) +
            (this[index + 4].toULong() shl 24) + (this[index + 5].toULong() shl 16) + (this[index + 6].toULong() shl 8) + (this[index + 7].toULong().ui)).ul
}

fun ByteArray.readAll(index: Int = 0): ByteArray = copyOfRange(index, size)


package com.yoavst.skaty.utils

import unsigned.*

fun Boolean.toInt() = if (this) 1 else 0
fun Boolean.toByte() = toInt().toByte()

fun Ushort.clearLeftBits(count: Int) = (this shl count) shr count
fun Ubyte.clearLeftBits(count: Int) = (this shl count) shr count
fun Int.clearLeftBits(count: Int) = (this shl count) ushr count

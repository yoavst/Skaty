package com.yoavst.skaty.utils

import unsigned.*

internal fun Boolean.toInt() = if (this) 1 else 0
internal fun Boolean.toByte() = toInt().toByte()

internal fun Ushort.clearLeftBits(count: Int) = (this shl count) shr count
internal fun Ubyte.clearLeftBits(count: Int) = (this shl count) shr count
internal fun Int.clearLeftBits(count: Int) = (this shl count) ushr count

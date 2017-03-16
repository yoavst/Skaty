package com.yoavst.skaty.protocols

import unsigned.Uint
import unsigned.Ulong
import unsigned.toUint
import unsigned.toUlong
import java.math.BigInteger

internal fun String.toIpAddress() = split('.').foldIndexed(0L) { index, result, item -> result or (item.toLong() shl ((3 - index) * 8)) }.toUint()

internal fun Uint.toFormattedIpAddress(): String {
    return "${(this shr 24) and 0xFF}.${(this shr 16) and 0xFF}.${(this shr 8) and 0xFF}.${this and 0xFF}"
}

internal fun String.toMacAddress() = BigInteger(replace("-", ""), 16).toUlong()

internal fun Ulong.toFormattedMacAddress(): String {
    return ((this shr 40).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 32).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 24).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 16).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 8).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 0).toUbyte().toInt().toString(16).padStart(2, '0')).toUpperCase()
}
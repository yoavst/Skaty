package com.yoavst.skaty.protocols

import com.yoavst.skaty.serialization.readShort
import java.math.BigInteger
import org.pcap4j.util.ByteArrays.SHORT_SIZE_IN_BYTES
import org.pcap4j.util.ByteArrays.BYTE_SIZE_IN_BITS
import unsigned.*


internal fun String.toIpAddress() = split('.').foldIndexed(0L) { index, result, item -> result or (item.toLong() shl ((3 - index) * 8)) }.toUint()

internal fun Uint.toFormattedIpAddress(): String {
    return "${(this shr 24) and 0xFF}.${(this shr 16) and 0xFF}.${(this shr 8) and 0xFF}.${this and 0xFF}"
}

internal fun String.toMacAddress() = BigInteger(replace("-", "").replace(":","").toLowerCase(), 16).toUlong()

internal fun Ulong.toFormattedMacAddress(): String {
    return ((this shr 40).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 32).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 24).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 16).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 8).toUbyte().toInt().toString(16).padStart(2, '0') + "-" +
            (this shr 0).toUbyte().toInt().toString(16).padStart(2, '0')).toUpperCase()
}

/**
 * A utility method to calculate the Internet checksum.

 * @see [RFC 1071](https://tools.ietf.org/html/rfc1071)

 * @param data data
 * *
 * @return checksum
 */
internal fun calcChecksum(data: ByteArray): Short {
    var sum: Long = 0
    var i = 1
    while (i < data.size) {
        sum += data.readShort(i - 1).toULong()
        i += SHORT_SIZE_IN_BYTES
    }
    if (data.size % 2 != 0) {
        sum += 0xFFFFL and (data[data.size - 1].toUbyte() shl BYTE_SIZE_IN_BITS)
    }

    while (sum shr BYTE_SIZE_IN_BITS * SHORT_SIZE_IN_BYTES != 0L) {
        sum = (0xFFFFL and sum) + sum.ushr(BYTE_SIZE_IN_BITS * SHORT_SIZE_IN_BYTES)
    }

    return sum.inv().toShort()
}
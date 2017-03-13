package com.yoavst.skaty.utils

import unsigned.*

object Struct {
    // not supported: l, L, q, Q, f, d, s, P
    fun parse(orders: String, data: ByteArray): List<Any> {
        try {
            var index = 0
            val results = mutableListOf<Any>()
            for (order in orders) {
                when (order) {
                    'c' -> {
                        results += data[index].toChar()
                        index++
                    }
                    'b' -> {
                        results += data[index]
                        index++
                    }
                    'B' -> {
                        results += data[index].toUbyte()
                        index++
                    }
                    '?' -> {
                        results += data[index].toInt() != 0
                        index++
                    }
                    'h' -> {
                        results += ((data[index].toInt() shl 8) + data[index + 1].toInt()).toShort()
                        index += 2
                    }
                    'H' -> {
                        results += ((data[index].toInt() shl 8) + data[index + 1].toInt()).us
                        index += 2
                    }
                    'i' -> {
                        results += ((data[index].toInt() shl 24) + (data[index + 1].toInt() shl 16) + (data[index + 2].toInt() shl 8) + data[index + 3].ui)
                        index += 4
                    }
                    'I' -> {
                        results += ((data[index].toInt() shl 24) + (data[index + 1].toInt() shl 16)
                                + (data[index + 2].toInt() shl 8) + data[index + 3].toInt()).ui
                        index += 4
                    }
                    'p' -> {
                        val length = (data[index].us shl 8) + data[index + 1].us
                        index += 2
                        results += String(data.sliceArray(index until index + length), Charsets.US_ASCII)
                        index += length
                    }
                    'P' -> {
                        results += data.sliceArray(index..data.size)
                        index = data.size
                    }

                }
            }
            return results
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
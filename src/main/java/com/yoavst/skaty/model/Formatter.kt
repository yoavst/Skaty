package com.yoavst.skaty.model

import unsigned.Ubyte
import unsigned.Ushort
import kotlin.reflect.KClass

interface Formatter<in K : Any> {
    fun format(value: K?): String
}

/**
 * Define the field to be formatted with a [Formatter]. [clazz] has to represent a [Formatter].
 *
 * **Note:** [clazz] must be `object`.
 */
annotation class Formatted(val clazz: KClass<*>)

inline fun <K : Any> formatter(crossinline func: (K?) -> String): Formatter<K> = object : Formatter<K> { override fun format(value: K?): String = func(value) }

object UshortHexFormatter : Formatter<Ushort> {
    override fun format(value: Ushort?): String {
        if ((value ?: 0).toInt() == 0) return "0"
        return "0X" + value!!.toUint().toInt().toString(16).toUpperCase()
    }
}

object UByteHexFormatter : Formatter<Ubyte> {
    override fun format(value: Ubyte?): String {
        if ((value ?: 0).toInt() == 0) return "0"
        return "0X" + value!!.toUint().toInt().toString(16).toUpperCase()
    }
}
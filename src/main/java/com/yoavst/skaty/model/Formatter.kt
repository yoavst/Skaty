package com.yoavst.skaty.model

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
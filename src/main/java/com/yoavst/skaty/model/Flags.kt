package com.yoavst.skaty.model

class Flags<K : Enum<K>>(private val items: Set<K> = mutableSetOf()) : Set<K> by items {
    override fun toString(): String = "(" + items.joinToString { it.name } + ")"
}

fun <K : Enum<K>> flagsOf(vararg items: K) = Flags(items.toMutableSet())
fun <K : Enum<K>> emptyFlags() = flagsOf<K>()
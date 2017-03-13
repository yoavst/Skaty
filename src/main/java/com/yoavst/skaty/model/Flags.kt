package com.yoavst.skaty.model

class Flags<K : Enum<K>>(private val items: Set<K> = mutableSetOf()) : Set<K> by items {
    override fun toString(): String = joinToString(prefix = "[", postfix = "]") { it.name }

    override fun equals(other: Any?): Boolean {
        if (other !is Flags<*>) return false
        if (other.size != size) return false
        return containsAll(other)
    }

    override fun hashCode(): Int = fold(1) { total, item -> 31 * total + item.hashCode() }
}

fun <K : Enum<K>> flagsOf(vararg items: K) = Flags(items.toMutableSet())
fun <K : Enum<K>> emptyFlags() = flagsOf<K>()
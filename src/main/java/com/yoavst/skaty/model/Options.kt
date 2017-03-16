package com.yoavst.skaty.model

import com.yoavst.skaty.protocols.declarations.IProtocol

class Options<Option : IProtocol<Option>>(private val data: List<Option>) : List<Option> by data {
    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        if (isEmpty()) return "[]"
        val formatter = first().marker as? Formatter<Option>
        return joinToString(prefix = "[", postfix = "]") {
            formatter?.format(it) ?: it.toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Options<*>) return false
        if (other.size != size) return false
        repeat(size) {
            if (this[it] != other[it]) return false
        }
        return true
    }

    override fun hashCode(): Int = fold(1) { total, item -> 31 * total + (item.hashCode()) }

    companion object {
        fun <K : IProtocol<K>> of(vararg options: K) = Options(options.toList())
        fun <K : IProtocol<K>> empty() = of<K>()

    }
}



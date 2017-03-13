package com.yoavst.skaty.model

abstract class BaseOptions<T : BaseOptions<T, Option>, Option> : List<Option> {
    override fun toString(): String = joinToString(prefix = "[", postfix = "]")
    override fun equals(other: Any?): Boolean {
        if (other !is BaseOptions<*, *>) return false
        if (other.size != size) return false
        repeat(size) {
            if (this[it] != other[it]) return false
        }
        return true
    }

    override fun hashCode(): Int = fold(1) { total, item -> 31 * total + (item?.hashCode() ?: 0) }
}

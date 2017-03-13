package com.yoavst.skaty.field

abstract class Field<K>(val name: String, val default: K, var value: K) {
    operator fun invoke() = value
    operator fun component1() = value

    fun setIf(data: K?) {
        if (data != null)
            value = data
    }

    fun del() {
        value = default
    }

    override fun toString() = value.toString()
    override fun equals(other: Any?): Boolean = other === this || (other is Field<*> && other.value == value)

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (default?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }


}

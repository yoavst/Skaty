package com.yoavst.skaty.model

abstract class BaseOptions<T : BaseOptions<T, Option>, Option> : List<Option> {
    override fun toString(): String = joinToString(prefix = "[", postfix = "]")
}

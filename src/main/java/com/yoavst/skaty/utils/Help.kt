package com.yoavst.skaty.utils

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.protocols.IProtocolMarker
import com.yoavst.skaty.protocols.IProtocolOption
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

object Help : BasePrinter() {
    var ParameterNameColor: Color = Color.BLACK
    var FieldColor: Color = Color.RED
    var DefaultValueColor: Color = Color.CYAN


    fun generate(protocol: IProtocolMarker<*>): String {
        val defaultValue = protocol.defaultValue
        val properties = defaultValue::class.memberProperties.filter { it.findAnnotation<Exclude>() == null && it.name !in ExcludedNames}
        if (properties.isNotEmpty()) {
            val info = properties.map { Triple(it.name, it.returnType.format(), it.getFormatter().format(it.getter.call(defaultValue))) }
            val maxNameLen = info.maxBy { (name, _, _) -> name.length }!!.first.length + 1
            val maxTypeLen = info.maxBy { (_, type, _) -> type.length }!!.second.length + 1
            return info.joinToString(separator = "\n") { (name, type, value) ->
                "${name.padEnd(maxNameLen).colorize(ParameterNameColor)} : ${type.padEnd(maxTypeLen).colorize(FieldColor)} = (${value.colorize(DefaultValueColor)})"
            }
        }
        return ""
    }

    fun optionGenerate(option: KClass<IProtocolOption<*>>): String {
        val properties = option.declaredMemberProperties.filter { it.findAnnotation<Exclude>() == null && it.name !in ExcludedNames }
        if (properties.isNotEmpty()) {
            val info = properties.map { it.name to it.returnType.format() }
            val maxNameLen = info.maxBy { (name, _) -> name.length }!!.first.length + 1
            val maxTypeLen = info.maxBy { (_, type) -> type.length }!!.second.length + 1
            return info.joinToString(separator = "\n") { (name, type) ->
                "${name.padEnd(maxNameLen).colorize(ParameterNameColor)} : ${type.padEnd(maxTypeLen).colorize(FieldColor)}"
            }
        }
        return ""
    }



    private fun KType.format(): String = toString().cleanTypeName()

    private fun String.cleanTypeName(): String {
        val indexGenerics = indexOf('<')
        return if (indexGenerics < 0) {
            // no generics
            val index = lastIndexOf('.')
            if (index < 0) this else substring(index + 1)
        } else {
            val index = lastIndexOf('.', startIndex = indexGenerics)
            if (index < 0) substring(0, indexGenerics) + "<" + substring(indexGenerics + 1, length - 1).cleanTypeName() + ">"
            else substring(index + 1, indexGenerics) + "<" + substring(indexGenerics + 1, length - 1).cleanTypeName() + ">"
        }
    }
}
package com.yoavst.skaty.utils

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.Formatter
import com.yoavst.skaty.model.formatter
import com.yoavst.skaty.protocols.default
import com.yoavst.skaty.protocols.interfaces.IContainerProtocol
import com.yoavst.skaty.protocols.interfaces.IProtocol
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

object ToString {
    private val defaultFormatter: Formatter<Any> = formatter(Any?::toString)

    var IsColorEnabled: Boolean = true
    var ProtocolColor: Color = Color.RED
    var ParameterNameColor: Color = Color.BLACK
    var ParameterValueColor: Color = Color.CYAN

    fun generate(protocol: IProtocol<*>, builder: StringBuilder = StringBuilder()): String {
        val properties = protocol::class.declaredMemberProperties.filter { it.findAnnotation<Exclude>() == null && it.name != "payload" && it.name != "marker" }
        builder.apply {
            append("<").append(protocol.marker.name.colorize(ProtocolColor)).append(' ')
            for (property in properties) {
                val value = property.getter.call(protocol)
                if (value != property.default<Any?>(protocol)) {
                    val formatter = property.getFormatter()
                    append(property.name.colorize(ParameterNameColor)).append('=').append(formatter.format(value).colorize(ParameterValueColor)).append(' ')
                }
            }
            append('|')
            if (protocol is IContainerProtocol<*>) {
                val payload = protocol.payload
                if (payload != null) {
                    generate(payload, builder)
                }
            }
            append('>')
        }
        return builder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<out IProtocol<*>, Any?>.getFormatter(): Formatter<Any> {
        return returnType.jvmErasure.findAnnotation<Formatted>()?.clazz?.companionObjectInstance as? Formatter<Any> ?:
                findAnnotation<Formatted>()?.clazz?.objectInstance as? Formatter<Any> ?:
                defaultFormatter
    }

    private fun String.colorize(color: Color) = if (IsColorEnabled) color.value + this + ANSI_RESET else this
}

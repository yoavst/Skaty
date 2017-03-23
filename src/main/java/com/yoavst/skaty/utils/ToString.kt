package com.yoavst.skaty.utils

import com.yoavst.skaty.model.Exclude
import com.yoavst.skaty.protocols.*
import kotlin.reflect.full.findAnnotation

object ToString : BasePrinter() {
    var ProtocolColor: Color = Color.RED
    var ParameterNameColor: Color = Color.BLACK
    var ParameterValueColor: Color = Color.CYAN

    fun generate(protocol: IProtocol<*>, builder: StringBuilder = StringBuilder()): String {
        val properties = protocol::class.getFields().filter { it.findAnnotation<Exclude>() == null && it.name !in ExcludedNames }
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



    private val hexArray = "0123456789ABCDEF".toCharArray()
    fun toHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt()
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}

package com.yoavst.skaty.serialization

import com.yoavst.skaty.protocols.*
import unsigned.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

object DefaultSerializationEnvironment : SerializationContext {
    private val bindingProperties: MutableMap<KProperty<*>, MutableMap<Any?, IProtocolMarker<*>>> = mutableMapOf()
    fun bind(property: KProperty<*>, value: Any?, protocol: IProtocolMarker<*>) {
        val valuesMap = bindingProperties[property]
        if (valuesMap == null) {
            bindingProperties[property] = mutableMapOf(value to protocol)
        } else {
            if (value !in valuesMap) {
                valuesMap[value] = protocol
            }
        }
    }

    init {
        bind(Ether::type, 2048.us, IP)

        bind(IP::proto, 4.ub, IP)
        bind(IP::proto, 6.ub, TCP)
        bind(IP::proto, 17.ub, UDP)
    }


    override fun serialize(reader: SimpleReader, parent: IContainerProtocol<*>?): IProtocol<*>? {
        if (reader.hasMore()) {
            if (parent == null) {
                return Raw(String(reader.readAsByteArray(), Charsets.US_ASCII))
            } else {
                val properties = parent::class.memberProperties
                for (property in bindingProperties.keys) {
                    if (property in properties) {
                        val valuesMap = bindingProperties[property]!!
                        val protocol = valuesMap[property.getter.call(parent)]
                        if (protocol != null) {
                            val result = protocol.of(reader, this)
                            if (result != null) return result
                        }
                    }
                }
                return Raw(String(reader.readAsByteArray(), Charsets.US_ASCII))
            }
        } else return null
    }
}
package com.yoavst.skaty.serialization

import com.yoavst.skaty.pcap.Pcap
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.serialization.SerializationContext.Stage
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
        bind(Pcap::dataLink, 1.ui, Ether)

        bind(Ether::type, 2048.us, IP)

        bind(IP::proto, 4.ub, IP)
        bind(IP::proto, 6.ub, TCP)
        bind(IP::proto, 17.ub, UDP)
    }


    override fun deserialize(reader: SimpleReader, parent: IProtocol<*>?): IProtocol<*>? {
        if (reader.hasMore()) {
            if (parent == null) {
                return Raw(reader.readAsByteArray())
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
                return Raw(reader.readAsByteArray())
            }
        } else return null
    }

    override fun serialize(protocol: IProtocol<*>, writer: SimpleWriter, stage: Stage) {
        if (stage == Stage.Checksum) {
            if (protocol is IContainerProtocol<*>)
                protocol.payload?.let { serialize(it, writer, stage) }
            protocol.write(writer, stage)
        } else {
            @Suppress("NAME_SHADOWING")
            var p: IProtocol<*>? = protocol
            while (true) {
                if (p == null)
                    break

                p.write(writer, stage)
                p = (p as? IContainerProtocol<*>)?.payload
            }
            val next = stage.next()
            if (next != Stage.Checksum)
                writer.index = 0
            if (next != null)
                serialize(protocol, writer, next)
        }
    }
}
package com.yoavst.skaty.serialization

import com.yoavst.skaty.protocols.IProtocol

interface SerializationContext {
    fun deserialize(reader: SimpleReader, parent: IProtocol<*>? = null): IProtocol<*>?
    fun serialize(protocol: IProtocol<*>, writer: SimpleWriter, stage: Stage)

    enum class Stage {
        Data, Length, Checksum;

        fun next(): Stage? = if (this == Data) Length else if (this == Length) Checksum else null
    }
}
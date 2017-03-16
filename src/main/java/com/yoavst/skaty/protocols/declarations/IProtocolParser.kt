package com.yoavst.skaty.protocols.declarations

import com.yoavst.skaty.serialization.DefaultSerializationEnvironment
import com.yoavst.skaty.serialization.SerializationContext
import com.yoavst.skaty.serialization.SimpleReader

interface IProtocolParser<K : IProtocol<K>> {
    /**
     * Try to parse the data into the protocol [K]
     */
    fun of(reader: SimpleReader, serializationContext: SerializationContext = DefaultSerializationEnvironment): K?

    operator fun invoke(reader: SimpleReader): K = of(reader) ?: throw IllegalArgumentException("reader does not represent the protocol.")
}
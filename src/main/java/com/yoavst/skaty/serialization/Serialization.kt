package com.yoavst.skaty.serialization

import com.yoavst.skaty.protocols.IProtocol

interface Serialization<K : IProtocol<*>> {
    fun serialize(protocol: K): ByteArray
    fun deserialize(array: ByteArray): K
}
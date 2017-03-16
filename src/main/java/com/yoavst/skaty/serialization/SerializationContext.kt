package com.yoavst.skaty.serialization

import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.IContainerProtocol

interface SerializationContext {
    fun serialize(reader: SimpleReader, parent: IContainerProtocol<*>? = null): IProtocol<*>?
}
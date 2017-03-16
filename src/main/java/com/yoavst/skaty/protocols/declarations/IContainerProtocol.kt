package com.yoavst.skaty.protocols.declarations

import com.yoavst.skaty.model.Exclude

/**
 * A protocol that may contain another protocol as its payload.
 */
interface IContainerProtocol<K : IContainerProtocol<K>> : IProtocol<K> {
    /**
     * The payload of the protocol.
     */
    @Exclude
    var payload: IProtocol<*>?
}
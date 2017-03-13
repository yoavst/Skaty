package com.yoavst.skaty.protocols

/**
 * A protocol that may contain another protocol as its payload.
 */
interface IContainerProtocol<K : IContainerProtocol<K>> : IProtocol<K> {
    /**
     * The payload of the protocol.
     */
    var payload: IProtocol<*>?
}
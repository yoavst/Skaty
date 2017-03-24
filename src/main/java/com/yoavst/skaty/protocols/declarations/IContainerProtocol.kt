@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

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
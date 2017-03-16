package com.yoavst.skaty.protocols.declarations

import com.yoavst.skaty.model.Exclude

interface IProtocol<K : IProtocol<K>> {
    /**
     * The marker is the static extension for the protocol.
     */
    @Exclude
    val marker: IProtocolMarker<K>
}

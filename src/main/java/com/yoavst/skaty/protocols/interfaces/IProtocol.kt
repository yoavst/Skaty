package com.yoavst.skaty.protocols.interfaces

interface IProtocol<K : IProtocol<K>> : Cloneable {
    /**
     * The marker is the static extension for the protocol.
     */
    val marker: IProtocolMarker<K>
    /**
     * Clone the protocol
     */
    public override fun clone(): K
}

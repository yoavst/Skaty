package com.yoavst.skaty.protocols.declarations

interface IProtocolOption<T : IProtocol<T>> : IProtocol<T> {
    // Option doesn't have a parent
    override var parent: IProtocol<*>?
        get() = null
        set(value) {}
}
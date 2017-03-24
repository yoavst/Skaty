package com.yoavst.skaty.protocols

abstract class BaseProtocol<T : BaseProtocol<T>> : IContainerProtocol<T> {
    abstract protected var _payload: IProtocol<*>?

    override final var payload: IProtocol<*>?
        get() = _payload
        set(value) {
            _payload = value
            value?.parent = this
            onPayload()
        }

    open fun onPayload() = Unit
}
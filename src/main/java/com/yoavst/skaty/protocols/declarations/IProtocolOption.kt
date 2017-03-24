@file:Suppress("PackageDirectoryMismatch")

package com.yoavst.skaty.protocols

interface IProtocolOption<T : IProtocol<T>> : IProtocol<T> {
    // Option doesn't have a parent
    override var parent: IProtocol<*>?
        get() = null
        set(value) {}
}
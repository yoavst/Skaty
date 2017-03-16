package com.yoavst.skaty.protocols

import com.yoavst.skaty.protocols.declarations.IContainerProtocol

abstract class BaseProtocol<T : BaseProtocol<T>> : IContainerProtocol<T>
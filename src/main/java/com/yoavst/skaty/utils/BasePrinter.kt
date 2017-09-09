package com.yoavst.skaty.utils

import com.yoavst.skaty.model.Formatted
import com.yoavst.skaty.model.Formatter
import com.yoavst.skaty.model.formatter
import com.yoavst.skaty.protocols.IContainerProtocol
import com.yoavst.skaty.protocols.IProtocol
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

open class BasePrinter {
    var IsColorEnabled: Boolean = true
    private val defaultFormatter: Formatter<Any> = formatter(Any?::toString)

    @Suppress("UNCHECKED_CAST")
    protected fun KProperty1<*, Any?>.getFormatter(): Formatter<Any> {
        return returnType.jvmErasure.findAnnotation<Formatted>()?.clazz?.companionObjectInstance as? Formatter<Any> ?:
                findAnnotation<Formatted>()?.clazz?.objectInstance as? Formatter<Any> ?:
                defaultFormatter
    }

    protected fun String.colorize(color: Color) = if (ToString.IsColorEnabled) color.value + this + ANSI_RESET else this

    protected fun KClass<*>.getFields(): Collection<KProperty1<*, *>> {
        if (this == Any::class) return emptyList()
        return declaredMemberProperties + supertypes.flatMap { it.jvmErasure.getFields() }
    }

    protected val ExcludedNames = arrayOf(
            IContainerProtocol<*>::payload.name,
            "_payload",
            IProtocol<*>::marker.name,
            IProtocol<*>::parent.name
    )
}
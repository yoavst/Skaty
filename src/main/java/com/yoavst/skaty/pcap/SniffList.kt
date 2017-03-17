package com.yoavst.skaty.pcap

import com.yoavst.skaty.protocols.IContainerProtocol
import com.yoavst.skaty.protocols.IProtocol
import com.yoavst.skaty.protocols.Raw

class SniffList(var name: String, private val data: List<IProtocol<*>>) : List<IProtocol<*>> by data {
    override fun toString(): String {
        val counts = mutableMapOf<String, Int>()
        data.asSequence()
                .map { it.markerName() }
                .forEach { counts[it] = counts.getOrDefault(it, 0) + 1 }

        return "<$name: ${counts.toList().joinToString(separator = " ") { (name, count) -> "$name:$count" }} />"
    }

    private fun IProtocol<*>.markerName(): String {
        if (this is IContainerProtocol<*> && payload != null && !Raw.isProtocol(payload!!))
            return payload!!.markerName()
        else
            return marker.name
    }
}
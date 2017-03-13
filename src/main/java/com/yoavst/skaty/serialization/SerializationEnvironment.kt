package com.yoavst.skaty.serialization

import java.io.Closeable

object SerializationEnvironment {
    private val bufferPool: BufferPool = BufferPool(1500, 65536, 10)

    fun reader(byteArray: ByteArray): SimpleReader = ByteArraySimpleReader(byteArray)
    fun writer(maxSize: Int): SimpleWriter {
        val buffer = bufferPool.acquire(maxSize)
        return ByteArraySimpleWriter(buffer, Closeable { bufferPool.release(buffer) })
    }
}
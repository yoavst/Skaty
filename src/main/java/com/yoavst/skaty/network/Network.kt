package com.yoavst.skaty.network

import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.serialization.ByteArraySimpleReader
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode
import org.pcap4j.core.Pcaps
import unsigned.ul
import java.io.Closeable
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread


object Network : Closeable {
    private lateinit var readHandle: PcapHandle
    private lateinit var sendHandle: PcapHandle
    private var address: InetAddress = InetAddress.getLocalHost()

    internal var index: AtomicLong = AtomicLong(0)
    internal var currentPacket: Pair<IProtocol<*>, Long> = Raw(ByteArray(0)) to index.get()

    val macAddress: Ether.MAC
        get() = mac(NetworkInterface.getByInetAddress(address).hardwareAddress)

    val ipAddress: IP.Address
        get() = ip(address.address)

    fun init(inetAddress: InetAddress) {
        address = inetAddress
        init()
    }

    fun init() {
        val nif = Pcaps.getDevByAddress(address)
        sendHandle = nif.openLive(65536, PromiscuousMode.NONPROMISCUOUS, 0)
        Runtime.getRuntime().addShutdownHook(Thread {
            close()
        })
        thread(start = true, isDaemon = false) {
            readHandle = nif.openLive(65536, PromiscuousMode.NONPROMISCUOUS, 0)
            while (readHandle.isOpen) {
                val raw = readHandle.nextRawPacket
                if (raw != null) {
                    val packet = Ether.of(ByteArraySimpleReader(raw))
                    if (packet != null)
                        currentPacket = packet to index.incrementAndGet()
                }
            }
        }
    }

    override fun close() {
        readHandle.close()
        sendHandle.close()
    }

    fun sniff(timeout: Long = Long.MAX_VALUE): Sequence<IProtocol<*>> {
        val startTime: Long = System.currentTimeMillis()
        var iteratorIndex: Long = index.get()
        return generateSequence {
            while (true) {
                val snifferIndex = index.get()
                if (snifferIndex > 0 && snifferIndex > iteratorIndex) break
                else if (System.currentTimeMillis() - startTime >= timeout)
                    return@generateSequence null
            }
            val (packet, newIndex) = currentPacket
            iteratorIndex = newIndex
            packet
        }
    }

    fun sendp(packet: Ether) {
        sendHandle.sendPacket(packet.toByteArray())
    }

    fun send(packet: IP) {
        sendHandle.sendPacket((Ether() / packet).toByteArray())
    }
}
![Logo](logo.png)

[![Kotlin 1.1.1](https://img.shields.io/badge/Kotlin-1.1.1-blue.svg)](http://kotlinlang.org)
![Version Alpha](https://img.shields.io/badge/Version-alpha-yellow.svg) 
[![License Apache](https://img.shields.io/badge/License-Apache%202.0-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Skaty
=====

Introducing Skaty, the Kotlin port for [Scapy](http://www.secdev.org/projects/scapy/). Skaty is a POC therefore it lacks many important features. Do not use on production!

```kotlin
import com.yoavst.skaty.protocols.*
import com.yoavst.skaty.network.Network.*

fun showcase() {
    val packet = IP(dst = ip("192.168.1.1"), options = optionsOf(IPOption.MTUProb(22.us))) / 
        UDP(sport = 7000.us, dport = 7000.us) / "Hello world"
    
    // work with properties
    packet.dst = mac("AA-BB-CC-DD-EE-FF")
    del(packet::dst)
    println(packet)
        
    // get a layer
    val tcp = packet[TCP]
    println(tcp.dport)
    println(UDP in packet)
    
    // send packet
    sendp(packet)
    
    // sniff packets
    val packets = sniff(timeout = 2000).filter { TCP in it && it[TCP].dport == 1200.us }.take(10).map { item -> item[TCP].ack }.toList()
    packets.forEach(::println)
}
```

## Features
- [X] Create Ether, IP, TCP, UDP or Raw packets programmatically.
- [X] Create new protocols and integrate them with the existing framework
- [X] Serialize data to byte array
- [X] Deserialize raw data to protocols using extensible deserializer.
- [X] Read pcap files
- [X] Send packets and sniff incoming packets
- [ ] Sending and sniffing uses pcap4j and not self implementation
- [ ] More protocols are needed
- [ ] Serialization model need to be changed

## Initialization
Currently, in order to initialize the library, you have to call `Network.init(String)` with the IP address of the network interface.

```kotlin
init("192.168.1.5")
```

This call will make a daemon process. In order to close it, use `Network.close()`.

## Create packet
Just do the same as you would have done in scapy. 

**Note:** The library use [kotlin-unsigned](https://github.com/kotlin-graphics/kotlin-unsigned), so you need to postfix your numbers with `.ub, .us, .ui` or `.ul`.

```kotlin
 TCP(dport = 80.us, sport = 1200.us, flags = flagsOf(SYN, ACK), options = optionsOf(TCPOption.NOP, TCPOption.Timestamp(1489416311.ui, 1.ui)))
```

Use `flagsOf(varargs Flag)` for flags and `optionsOf(vararg Option)` for options. `mac(String)` and `ip(String)` provide easy way to use MAC & IP addresses.

## Send packet
Same as scapy. **Note:** `sr1` and `srp1` are not available yet
```kotlin
sendp(etherPacket)
send(ipPacket)
```

## Read pcap file
```kotlin
val sniff = pcapOf("sample.pcap")
sniff.filter { it.time > 50 && UDP in it && it[UDP].dport == 53 }.forEach(::println)
```

## Sniffing
```kotlin
val packets = sniff(timeout = 2000).filter { TCP in it && it[TCP].dport == 1200.us }.take(10).toList()
```


# License

    Copyright 2017 Yoav Sternberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

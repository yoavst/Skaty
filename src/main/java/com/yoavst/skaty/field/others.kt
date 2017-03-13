package com.yoavst.skaty.field

import com.yoavst.skaty.protocols.impl.Ether
import com.yoavst.skaty.protocols.impl.IP

class StringField(name: String, default: String, value: String = default) : Field<String>(name, default, value)
class EnumField<K : Enum<K>>(name: String, default: K, value: K = default) : Field<K>(name, default, value)
class IPAddressField(name: String, default: IP.Address, value: IP.Address = default) : Field<IP.Address>(name, default, value)
class NullableIPAddressField(name: String, default: IP.Address?, value: IP.Address? = default) : Field<IP.Address?>(name, default, value)
class MACAddressField(name: String, default: Ether.MAC, value: Ether.MAC = default) : Field<Ether.MAC>(name, default, value)
class NullableMACAddressField(name: String, default: Ether.MAC?, value: Ether.MAC? = default) : Field<Ether.MAC?>(name, default, value)

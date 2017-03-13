package com.yoavst.skaty.field

import com.yoavst.skaty.model.BaseOptions
import com.yoavst.skaty.model.Flags
import unsigned.Ubyte
import unsigned.Uint
import unsigned.Ushort

class BitField(name: String, default: Boolean, value: Boolean = default) : Field<Boolean>(name, default, value)

class ByteField(name: String, default: Byte, value: Byte = default) : Field<Byte>(name, default, value)
class UByteField(name: String, default: Ubyte, value: Ubyte = default) : Field<Ubyte>(name, default, value)
class NullableUByteField(name: String, default: Ubyte?, value: Ubyte? = default) : Field<Ubyte?>(name, default, value)

class ShortField(name: String, default: Short, value: Short = default) : Field<Short>(name, default, value)
class UShortField(name: String, default: Ushort, value: Ushort = default) : Field<Ushort>(name, default, value)
class NullableUShortField(name: String, default: Ushort?, value: Ushort? = default) : Field<Ushort?>(name, default, value)

class UIntField(name: String, default: Uint, value: Uint = default) : Field<Uint>(name, default, value)
class IntField(name: String, default: Int, value: Int = default) : Field<Int>(name, default, value)

class FlagsField<K : Enum<K>>(name: String, default: Flags<K>, value: Flags<K> = default) : Field<Flags<K>>(name, default, value) {
    operator fun contains(item: K) = value.let { item in it }
}

class OptionsField<K : BaseOptions<K, T>, T>(name: String, default: K, value: K = default) : Field<K>(name, default, value)

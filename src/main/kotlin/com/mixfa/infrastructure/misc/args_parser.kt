package com.mixfa.infrastructure.misc

import kotlin.reflect.KClass

private const val SEPARATOR_BYTE = ':'.code.toByte()

private fun findNextSeparator(bytes: ByteArray, offset: Int, size: Int): Int {
    var i = offset
    while (i <= size) {
        val byte = bytes[i]
        if (byte == SEPARATOR_BYTE)
            return i

        ++i
    }
    return -1
}

private inline fun split(bytes: ByteArray, offset: Int, size: Int, handler: (offset: Int, size: Int) -> Unit) {
    var sep = findNextSeparator(bytes, offset, size)

    if (sep != -1) {
        if (sep != offset)
            handler(offset, sep - 1)
    } else {
        handler(offset, size - 1)
        return
    }

    while (sep != -1) {
        ++sep // skip delimiter char
        val nextSep = findNextSeparator(bytes, offset + sep, size - sep)
        if (nextSep != -1)
            handler(sep, nextSep - sep)
        else
            handler(sep, size - sep)

        sep = nextSep
    }
}

fun parseArgs(bytes: ByteArray, offset: Int, size: Int, argsTypes: List<KClass<*>>): Array<Any?> {
    if (argsTypes.isEmpty()) return emptyArray()

    val args = Array<Any?>(argsTypes.size) { null }

    var i = 0
    split(bytes, offset, size) { _offset, _size ->
        if (i == argsTypes.size) return args

        val arg = when (val type = argsTypes[i]) {
            String::class, Any::class -> String(bytes, _offset, _size)
            Int::class -> String(bytes, _offset, _size).toInt()
            Double::class -> String(bytes, _offset, _size).toDouble()
            Float::class -> String(bytes, _offset, _size).toFloat()
            Boolean::class -> bytes[_offset].compareTo(1)
            ByteArray::class -> bytes.copyOfRange(_offset, _offset + _size)
            else -> type.constructors.find { it.parameters.size == 1 && it.parameters[0] == String::class }
                ?.call(String(bytes, _offset, _size))
                ?: error("Unresolved type: $type")
        }

        args[i] = arg
        ++i
    }

    return args
}
//    val args = String(bytes, offset, size).split(':')
//
//    if (args.size != argsTypes.size) return null
//
//    return Array(argsTypes.size) { index ->
//        val type = argsTypes[index]
//        val arg = args[index]
//        when (type) {
//            String::class -> arg
//            Any::class -> arg
//            Int::class -> arg.toInt()
//            Double::class -> arg.toDouble()
//            Float::class -> arg.toFloat()
//            else -> type.constructors.find { it.parameters.size == 1 && it.parameters[0] == String::class }?.call(arg)
//                ?: error("Unresolved type: $type")
//        }
//    }

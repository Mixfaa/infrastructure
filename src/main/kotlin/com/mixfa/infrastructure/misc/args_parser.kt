package com.mixfa.infrastructure.misc

import kotlin.reflect.KClass

private fun findNextSeparator(bytes: ByteArray, offset: Int, size: Int): Int {
    var i = offset
    while (i <= size) {
        val byte = bytes[i]
        if (byte == PARAM_SEPARATOR_BYTE)
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

fun parseArgs(bytes: ByteArray, globalOffset: Int, globalSize: Int, argsTypes: List<KClass<*>>): Array<Any?> {
    if (argsTypes.isEmpty()) return emptyArray()

    val args = arrayOfNulls<Any?>(argsTypes.size)

    var i = 0
    split(bytes, globalOffset, globalSize) { offset, _size ->
        if (i == argsTypes.size) return args
        val size = if (i == argsTypes.lastIndex) globalSize - offset else _size

        val arg = when (val type = argsTypes[i]) {
            String::class, Any::class -> String(bytes, offset, size)
            Int::class -> String(bytes, offset, size).toInt()
            Double::class -> String(bytes, offset, size).toDouble()
            Float::class -> String(bytes, offset, size).toFloat()
            Boolean::class -> bytes[offset].compareTo(1)
            ByteArray::class -> bytes.copyOfRange(offset, offset + size)
            else -> type.constructors.find { it.parameters.size == 1 && it.parameters[0] == String::class }
                ?.call(String(bytes, offset, size))
                ?: error("Unresolved type: $type")
        }

        args[i] = arg
        ++i
    }

    return args
}

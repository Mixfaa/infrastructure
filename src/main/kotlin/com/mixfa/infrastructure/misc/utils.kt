package com.mixfa.infrastructure.misc

import java.nio.ByteBuffer

inline fun <T> ignoringExceptions(block: () -> T): T? {
    try {
        block()
    } catch (_: Exception) {
    }
    return null
}

fun String.toByteBuffer() : ByteBuffer = ByteBuffer.wrap(this.toByteArray())

const val PARAM_SEPARATOR_BYTE = ':'.code.toByte()
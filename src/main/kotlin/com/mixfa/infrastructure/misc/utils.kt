package com.mixfa.infrastructure.misc

import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer

fun String.toByteBuffer() : ByteBuffer = ByteBuffer.wrap(this.toByteArray())

const val PARAM_SEPARATOR_BYTE = ':'.code.toByte()

fun InvocationTargetException.throwUnwrapped() {
    throw this.targetException
}
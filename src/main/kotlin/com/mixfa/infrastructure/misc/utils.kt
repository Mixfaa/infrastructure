package com.mixfa.infrastructure.misc

import java.nio.ByteBuffer

fun String.toByteBuffer() : ByteBuffer = ByteBuffer.wrap(this.toByteArray())

const val PARAM_SEPARATOR_BYTE = ':'.code.toByte()
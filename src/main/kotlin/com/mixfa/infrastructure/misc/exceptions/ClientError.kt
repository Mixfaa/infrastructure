package com.mixfa.infrastructure.misc.exceptions

import com.mixfa.excify.FastException
import com.mixfa.infrastructure.misc.toByteBuffer

open class ClientError(msg: String) : FastException(msg) {
    val byteBuffered = msg.toByteBuffer()

    companion object {}
}

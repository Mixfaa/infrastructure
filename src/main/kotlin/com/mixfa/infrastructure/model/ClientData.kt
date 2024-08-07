package com.mixfa.infrastructure.model

import kotlinx.coroutines.CoroutineScope
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class ClientData(
    var user: User? = null,
    val socketChannel: AsynchronousSocketChannel,
    val coroutineScope: CoroutineScope,
    val buffer: ByteBuffer = ByteBuffer.allocate(512)
) {
    fun send(msg: ByteArray) {
        socketChannel.write(ByteBuffer.wrap(msg))
    }

    fun send(msg: String) {
        send(msg.toByteArray())
    }

    fun send(msg: ByteBuffer) {
        socketChannel.write(msg)
    }

    override fun toString(): String {
        return "ClientData(clientChannel=${socketChannel.remoteAddress})"
    }

    operator fun component1() = this
    operator fun component2() = this.coroutineScope

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientData) return false

        if (socketChannel != other.socketChannel) return false

        return true
    }

    override fun hashCode(): Int {
        return socketChannel.hashCode()
    }
}
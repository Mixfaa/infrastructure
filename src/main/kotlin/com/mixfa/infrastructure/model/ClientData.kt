package com.mixfa.infrastructure.model

import kotlinx.coroutines.CoroutineScope
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class ClientData(
    var user: User? = null,
    val clientChannel: AsynchronousSocketChannel,
    val coroutineScope: CoroutineScope,
    val buffer: ByteBuffer = ByteBuffer.allocate(512)
) {
    fun send(msg: ByteArray) {
        clientChannel.write(ByteBuffer.wrap(msg))
    }

    fun send(msg: String) {
        send(msg.toByteArray())
    }

    fun send(msg: ByteBuffer) {
        clientChannel.write(msg)
    }

    override fun toString(): String {
        return "ClientData(clientChannel=${clientChannel.remoteAddress})"
    }

    operator fun component1() = this
    operator fun component2() = this.coroutineScope

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientData) return false

        if (clientChannel != other.clientChannel) return false

        return true
    }

    override fun hashCode(): Int {
        return clientChannel.hashCode()
    }
}
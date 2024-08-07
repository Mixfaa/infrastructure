package com.mixfa.infrastructure.service

import com.mixfa.infrastructure.misc.Events
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

@Service
final class SocketChannelAcceptor(
    private val socketChannel: AsynchronousServerSocketChannel,
    private val socketChannelReader: SocketChannelReader,
    private val clientRegistry: ClientRegistry,
    private val logger: Logger
) : CompletionHandler<AsynchronousSocketChannel, Any?> {
    init {
        socketChannel.accept(null, this)

        Events.OnDisconnected.subscribe { client ->
            client.socketChannel.close()
        }
    }

    override fun completed(client: AsynchronousSocketChannel, attachment: Any?) {
        try {
            val clientData = clientRegistry.handleConnection(client)

            Events.OnConnected.emit(clientData)

            client.read(clientData.buffer, clientData, socketChannelReader)
        } catch (ex: Exception) {
            logger.error(ex.message ?: ex.localizedMessage ?: "Error while accepting connection: $ex")
        } finally {
            socketChannel.accept(null, this) // accept next
        }
    }

    override fun failed(exc: Throwable, attachment: Any?) {
        System.err.println("Failed to accept connection: $exc")
        exc.printStackTrace()
    }
}
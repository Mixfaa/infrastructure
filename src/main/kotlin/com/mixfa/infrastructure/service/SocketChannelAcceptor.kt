package com.mixfa.infrastructure.service

import com.mixfa.infrastructure.misc.event.OnClientConnected
import com.mixfa.infrastructure.misc.event.OnClientDisconnected
import org.slf4j.Logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

@Service
final class SocketChannelAcceptor(
    private val socketChannel: AsynchronousServerSocketChannel,
    private val socketChannelReader: SocketChannelReader,
    private val clientRegistry: ClientRegistry,
    private val logger: Logger,
    private val eventPublisher: ApplicationEventPublisher
) : CompletionHandler<AsynchronousSocketChannel, Any?>, ApplicationListener<OnClientDisconnected> {
    init {
        socketChannel.accept(null, this)
    }

    override fun completed(client: AsynchronousSocketChannel, attachment: Any?) {
        try {
            val clientData = clientRegistry.handleConnection(client)

            eventPublisher.publishEvent(OnClientConnected(clientData, this))

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

    override fun onApplicationEvent(event: OnClientDisconnected) {
        event.clientData.socketChannel.close()
    }
}
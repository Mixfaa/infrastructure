package com.mixfa.infrastructure.service

import arrow.core.nonFatalOrThrow
import com.mixfa.infrastructure.misc.Events
import com.mixfa.infrastructure.misc.exceptions.ClientError
import com.mixfa.infrastructure.misc.toByteBuffer
import com.mixfa.infrastructure.model.ClientData
import org.slf4j.Logger
import org.springframework.stereotype.Component
import java.nio.channels.CompletionHandler

@Component
class SocketChannelReader(
    private val messageHandler: MessageHandler,
    private val clientRegistry: ClientRegistry,
    private val logger: Logger
) : CompletionHandler<Int, ClientData> {

    private fun readNext(clientData: ClientData) {
        clientData.buffer.clear()
        clientData.clientChannel.read(
            clientData.buffer,
            clientData,
            this
        )
    }

    private fun emitDisconnection(clientData: ClientData) {
        clientRegistry.handleDisconnection(clientData)
        Events.OnDisconnected.emit(clientData)
    }

    override fun completed(result: Int, clientData: ClientData) {
        if (result == -1) {
            emitDisconnection(clientData)
            return
        }

        try {
            if (result != 0)
                messageHandler.redirect(
                    clientData,
                    clientData.buffer.array(),
                    result
                )
        } catch (ex: Throwable) {
            ex.nonFatalOrThrow()
            if (ex is ClientError) {
                logger.info("Client error ${ex.message}")
                ex.message?.let(clientData::send)
            } else {
                ex.printStackTrace()
                logger.error("Error: $ex")
                clientData.send(INTERNAL_SERVER_ERROR_MSG)
            }
        } finally {
            readNext(clientData)
        }
    }

    override fun failed(exc: Throwable?, clientData: ClientData) {
        logger.error("Failed to read message: $exc on $clientData")
        emitDisconnection(clientData)
    }

    companion object {
        private val INTERNAL_SERVER_ERROR_MSG = "Internal sever error".toByteBuffer()
    }
}
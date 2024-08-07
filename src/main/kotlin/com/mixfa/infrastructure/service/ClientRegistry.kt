package com.mixfa.infrastructure.service

import com.mixfa.infrastructure.model.ClientData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.springframework.stereotype.Service
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

@Service
class ClientRegistry(
    private val rootCoroutineContext: CoroutineContext
) {
    private val clients: MutableList<ClientData> = CopyOnWriteArrayList()

    fun handleDisconnection(client: ClientData) {
        client.socketChannel.close()
        client.coroutineScope.cancel()
        clients.remove(client)
    }

    fun handleConnection(socketChannel: AsynchronousSocketChannel): ClientData {
        val clientData = ClientData(
            socketChannel = socketChannel,
            coroutineScope = CoroutineScope(rootCoroutineContext + Job()),
        )

        clients.add(clientData)
        return clientData
    }
}
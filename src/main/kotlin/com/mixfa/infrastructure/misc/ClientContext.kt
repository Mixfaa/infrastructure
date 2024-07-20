package com.mixfa.infrastructure.misc

import com.mixfa.infrastructure.model.ClientData
import org.springframework.stereotype.Component

@Component
class ClientContextManager {
    private val currentClientData: ThreadLocal<ClientData> = ThreadLocal()

    fun put(clientData: ClientData) {
        if (currentClientData.get() != null) error("Client context is not empty")

        currentClientData.set(clientData)
    }

    fun get(): ClientData = currentClientData.get() ?: error("Client context is empty")

    fun clean() = currentClientData.remove()
}

@Component
class ClientContext(private val clientContextManager: ClientContextManager) {
    fun get(): ClientData = clientContextManager.get()
}
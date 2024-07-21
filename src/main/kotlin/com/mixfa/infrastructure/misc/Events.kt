package com.mixfa.infrastructure.misc

import com.mixfa.infrastructure.model.ClientData
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.PriorityBlockingQueue

typealias OnConnectedHandler = (ClientData) -> Unit
typealias OnDisconnectedHandler = (ClientData) -> Unit

open class GenericEvent<Handler> {
    protected val subscribers = CopyOnWriteArrayList<Handler>()

    fun subscribe(handler: Handler) {
        subscribers.add(handler)
    }
}

object Events {
    object OnConnected : GenericEvent<OnConnectedHandler>() {
        fun emit(clientData: ClientData) {
            for (subscriber in this.subscribers)
                subscriber(clientData)
        }
    }

    object OnDisconnected : GenericEvent<OnDisconnectedHandler>() {
        fun emit(clientData: ClientData) {
            for (subscriber in this.subscribers)
                subscriber(clientData)
        }
    }
}
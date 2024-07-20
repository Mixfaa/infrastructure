package com.mixfa.infrastructure.misc

import com.mixfa.infrastructure.model.ClientData
import java.util.concurrent.PriorityBlockingQueue

typealias OnConnectedHandler = (ClientData) -> Unit
typealias OnDisconnectedHandler = (ClientData) -> Unit

class PrioritizedHandler<T>(
    val priority: Int,
    val handler: T
) : Comparable<PrioritizedHandler<T>> {
    override fun compareTo(other: PrioritizedHandler<T>): Int = this.priority.compareTo(other.priority)
}

open class GenericEvent<Handler> {
    protected val subscribers = PriorityBlockingQueue<PrioritizedHandler<Handler>>()

    fun subscribe(priority: Int = 0, handler: Handler) {
        subscribers.add(PrioritizedHandler(priority, handler))
    }
}

object Events {
    object OnConnected : GenericEvent<OnConnectedHandler>() {
        fun emit(clientData: ClientData) {
            for (subscriber in this.subscribers)
                subscriber.handler(clientData)
        }
    }

    object OnDisconnected : GenericEvent<OnDisconnectedHandler>() {
        fun emit(clientData: ClientData) {
            for (subscriber in this.subscribers)
                subscriber.handler(clientData)
        }
    }
}
package com.mixfa.infrastructure.service.logic

import com.mixfa.infrastructure.misc.ClientContext
import com.mixfa.infrastructure.misc.Events
import com.mixfa.infrastructure.misc.exceptions.ChannelNotFoundException
import com.mixfa.infrastructure.misc.exceptions.ClientError
import com.mixfa.infrastructure.misc.toByteBuffer
import com.mixfa.infrastructure.model.ClientData
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface ChannelServiceOps {
    fun rentChannel(): String
    fun rentPublicChannel(name: String): String
    fun closeChannel(name: String)
    fun enterChannel(name: String)
    fun exitChannel(name: String)
    fun sendToChannel(name: String, payload: ByteArray)
    suspend fun listPublicChannels(query: String, page: Int): List<String>
}

data class Channel(
    @Volatile var admin: ClientData?,
    val name: String,
    val isPublic: Boolean = false,
    val participants: MutableList<ClientData> = CopyOnWriteArrayList(),
) {
    fun broadcast(msg: ByteArray, except: ClientData) {
        broadcast(ByteBuffer.wrap(msg), except)
    }

    fun broadcast(msg: ByteBuffer, except: ClientData) {
        if (admin !== except)
            admin?.send(msg)

        for (participant in participants) {
            if (participant !== except)
                participant.send(msg)
        }
    }
}

@Service
class ChannelService(
    private val clientContext: ClientContext,
    private val logger: Logger
) : ChannelServiceOps {
    private val channelsList: MutableList<Channel> = CopyOnWriteArrayList()

    init {
        Events.OnDisconnected.subscribe(0) { clientData ->
            for (channel in channelsList) {
                if (channel.admin === clientData)
                    channel.admin = null

                channel.participants.remove(clientData)

                if (channel.admin === null && channel.participants.isEmpty()) {
                    channelsList.remove(channel) // close empty channel
                    logger.info("Channel ${channel.name} closed")
                }
            }
        }
    }

    override fun rentChannel(): String {
        val client = clientContext.get()

        val channel = Channel(client, generateChannelName())
        channelsList.add(channel)

        return channel.name
    }

    override fun rentPublicChannel(name: String): String {
        val client = clientContext.get()

        var channel = channelsList.find { it.name == name }

        if (channel != null)
            throw ClientError("Channel name $name is taken")

        channel = Channel(client, name, true)
        channelsList.add(channel)

        return channel.name
    }

    override fun closeChannel(name: String) {
        val client = clientContext.get()

        val channel = channelsList.firstOrNull { it.name == name }
            ?: throw ChannelNotFoundException(name)

        if (channel.admin != client) throw ClientError("You are not admin of this channel")

        val closeChannelMessage = "close_channel:${channel.name}".toByteBuffer()
        for (participant in channel.participants)
            participant.send(closeChannelMessage)

        channelsList.remove(channel)
    }

    override fun enterChannel(name: String) {
        val client = clientContext.get()

        val channel = channelsList.find { it.name == name }
            ?: throw ChannelNotFoundException(name)

        channel.participants.add(client)
    }

    override fun exitChannel(name: String) {
        val client = clientContext.get()

        val channel = channelsList.find { it.name == name }
            ?: throw ChannelNotFoundException(name)

        channel.participants.remove(client)
    }

    override fun sendToChannel(name: String, payload: ByteArray) {
        val (client, scope) = clientContext.get()

        val channel = channelsList.find { it.name == name }
            ?: throw ChannelNotFoundException(name)

        scope.launch {
            val message = ByteBuffer.allocate(channel.name.length + payload.size + 1)
            message.put(channel.name.toByteArray())
            message.put(':'.code.toByte())
            message.put(payload)
            channel.broadcast(message, client)
        }
    }

    override suspend fun listPublicChannels(query: String, page: Int): List<String> {
        return channelsList.asSequence()
            .filter { it.isPublic && it.name.contains(query, true) }
            .drop(page * PAGE_SIZE)
            .take(PAGE_SIZE)
            .map(Channel::name)
            .toList()
    }

    companion object {
        private const val PAGE_SIZE = 20
        private val securedRandom = SecureRandom()

        @OptIn(ExperimentalEncodingApi::class)
        fun generateChannelName(): String = Base64.encode(ByteArray(32).apply(securedRandom::nextBytes))
    }
}
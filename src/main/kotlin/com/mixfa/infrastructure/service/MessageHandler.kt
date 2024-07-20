package com.mixfa.infrastructure.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mixfa.infrastructure.misc.exceptions.ClientError
import com.mixfa.infrastructure.misc.parseArgs
import com.mixfa.infrastructure.model.ClientData
import com.mixfa.infrastructure.service.logic.OperationBus
import com.mixfa.infrastructure.service.logic.Ops
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.callSuspend


@Component
class MessageHandler(
    private val bus: OperationBus,
    private val mapper: ObjectMapper
) {
    fun redirect(
        clientData: ClientData,
        bytes: ByteArray,
        size: Int
    ) {
        val tag = bytes.first()

        if (tag >= Ops.entries.size)
            throw ClientError("Invalid ops tag")

        val opsTag = Ops.entries[tag.toInt()]
        val args = parseArgs(bytes, 1, size, opsTag.args)

        if (args.size != opsTag.args.size)
            throw ClientError("Args mismatch, expected: ${opsTag.args}")

        if (opsTag.handler.isSuspend)
            handleAsync(clientData, opsTag, args)
        else
            handleSync(clientData, opsTag, args)
    }

    private fun sendResponse(clientData: ClientData, response: Any?) {
        if (response != null) {
            if (response is String)
                clientData.send(response)
            else if (response !is Unit)
                clientData.send(mapper.writeValueAsBytes(response))
        }
    }

    private fun handleAsync(clientData: ClientData, opsTag: Ops, args: Array<Any?>) {
        clientData.coroutineScope.launch {
            val response = try {
                opsTag.handler.callSuspend(bus, *args)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException // unwrap reflection exception
            }

            sendResponse(clientData, response)
        }
    }

    private fun handleSync(clientData: ClientData, opsTag: Ops, args: Array<Any?>) {
        val response = try {
            opsTag.handler.call(bus, *args)
        } catch (ex: InvocationTargetException) {
            throw ex.targetException // unwrap reflection exception
        }

        sendResponse(clientData, response)
    }

}
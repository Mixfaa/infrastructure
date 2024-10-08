package com.mixfa.infrastructure.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mixfa.infrastructure.misc.ClientContextManager
import com.mixfa.infrastructure.misc.exception.ClientError
import com.mixfa.infrastructure.misc.exception.invalidOpsTag
import com.mixfa.infrastructure.misc.parseArgs
import com.mixfa.infrastructure.misc.throwUnwrapped
import com.mixfa.infrastructure.misc.use
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
    private val mapper: ObjectMapper,
    private val clientContextManager: ClientContextManager
) {
    fun redirect(
        clientData: ClientData,
        bytes: ByteArray,
        size: Int
    ) {
        val tag = bytes.first()

        if (tag >= Ops.entries.size)
            throw ClientError.invalidOpsTag()

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
        if (response == null)
            return

        if (response is String) clientData.send(response)
        else if (response !is Unit) clientData.send(mapper.writeValueAsBytes(response))
    }

    private fun handleAsync(clientData: ClientData, opsTag: Ops, args: Array<Any?>) {
        clientData.coroutineScope.launch {
            try {
                val response = clientContextManager.use(clientData) {
                    opsTag.handler.callSuspend(bus, *args)
                }
                sendResponse(clientData, response)
            } catch (ex: InvocationTargetException) {
                ex.throwUnwrapped()
            }
        }
    }

    private fun handleSync(clientData: ClientData, opsTag: Ops, args: Array<Any?>) {
        try {
            val response = clientContextManager.use(clientData) {
                opsTag.handler.call(bus, *args)
            }
            sendResponse(clientData, response)
        } catch (ex: InvocationTargetException) {
            ex.throwUnwrapped()
        }
    }
}
package com.mixfa.infrastructure.service.logic

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

enum class Ops(val handler: KCallable<*>) {
    AUTHENTICATE(OperationBus::authenticate),
    REGISTER(OperationBus::register),
    RENT_CHANNEL(OperationBus::rentChannel),
    RENT_PUBLIC_CHANNEL(OperationBus::rentPublicChannel),
    CLOSE_CHANNEL(OperationBus::closeChannel),
    SUBSCRIBE_CHANNEL(OperationBus::enterChannel),
    UNSUBSCRIBE_CHANNEL(OperationBus::exitChannel),
    SEND_TO_CHANNEL(OperationBus::sendToChannel),
    LIST_PUBLIC_CHANNELS(OperationBus::listPublicChannels)
    ;

    val args: List<KClass<*>> = handler.parameters.asSequence()
        .drop(1) // drop this
        .map { it.type.jvmErasure }
        .toList()
}
package com.mixfa.infrastructure.misc.event

import com.mixfa.infrastructure.model.ClientData
import org.springframework.context.ApplicationEvent

class OnClientDisconnected(
    val clientData: ClientData,
    src: Any
) : ApplicationEvent(src)
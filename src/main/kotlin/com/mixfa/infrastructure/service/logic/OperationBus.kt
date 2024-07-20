package com.mixfa.infrastructure.service.logic

import org.springframework.stereotype.Component

@Component
class OperationBus(
    private val userService: UserServiceOps,
    private val channelService: ChannelServiceOps
) : UserServiceOps by userService,
    ChannelServiceOps by channelService {

    fun unimplemented() {
        println("unimplemented")
    }
}
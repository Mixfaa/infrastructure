package com.mixfa.infrastructure.misc.exceptions

import com.mixfa.infrastructure.service.logic.Channel

class ChannelNotFoundException(channelName: String) : ClientError("Channel $channelName not found")

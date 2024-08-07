package com.mixfa.infrastructure.misc.exception

class ChannelNotFoundException(channelName: String) : ClientError("Channel $channelName not found")

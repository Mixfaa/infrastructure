package com.mixfa.infrastructure.misc.exceptions

class ChannelNotFoundException(channelName: String) : ClientError("Channel $channelName not found")

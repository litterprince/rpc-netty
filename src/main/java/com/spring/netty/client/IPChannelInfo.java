package com.spring.netty.client;

import io.netty.channel.Channel;

public class IPChannelInfo {
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}

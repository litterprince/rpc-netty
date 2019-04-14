package com.spring.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

@Deprecated
public class IPChannelInfo {
    private Channel channel;

    private EventLoopGroup group;

    public IPChannelInfo() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if (group!=null && !group.isShutdown()) {
                        group.shutdownGracefully();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    public void setGroup(EventLoopGroup group) {
        this.group = group;
    }
}

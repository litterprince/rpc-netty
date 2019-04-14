package com.spring.netty.client;

import com.spring.netty.message.Request;
import com.spring.netty.message.Response;
import com.spring.netty.protocol.Protocol;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        Protocol protocol = (Protocol) msg;
        Response response = protocol.buildResponseByProtocol();

        assert response != null;
        Request request = RPCClient.getInstance().getRequestLockMap().get(response.getRequestId());
        synchronized (request){
            // TODO: 学习，唤醒客户端阻塞等待
            request.setResult(response.getResult());
            request.notifyAll();
        }
    }
}

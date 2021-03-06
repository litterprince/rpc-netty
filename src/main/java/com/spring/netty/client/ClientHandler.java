package com.spring.netty.client;

import com.spring.netty.message.Request;
import com.spring.netty.message.Response;
import com.spring.netty.promise.impl.Deferred;
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
        if (request != null) {
            synchronized (request) {
                request.setResult(response.getResult());
                request.notifyAll();
            }
        } else {
            Deferred deferred = RPCClient.getInstance().getPromiseMap().get(response.getRequestId());
            deferred.resolve(response.getResult());
        }
    }
}

package com.spring.netty.server;

import com.spring.netty.RPC;
import com.spring.netty.message.Request;
import com.spring.netty.message.Response;
import com.spring.netty.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * netty5.0：使用ChannelHandlerAdapter替换ChannelInboundHandler,ChannelOutboundHandler
 */
public class ServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Protocol protocol = (Protocol) msg;
        Request request = protocol.buildRequestByProtocol();
        Object result = invoke(request);

        // TODO：思考这里当时直接使用接收到的protocol传送回去会失败问题
        Protocol protocol1 = new Protocol();
        protocol1.buildRequestProtocol(request);
        Response response = new Response();
        response.setRequestId(request.getRequestId());
        response.setResult(result);
        protocol1.buildResponseProtocol(response);
        /*String respStr = RPC.responseEncode(response);
        ByteBuf responseBuf = Unpooled.copiedBuffer(respStr.getBytes());*/
        ctx.writeAndFlush(protocol1);
    }

    private static Object invoke(Request request){
        Object result = null;
        String implClassName = RPC.getServerConfig().getServerImplMap().get(request.getClassName());
        try {
            Class clazz = Class.forName(implClassName);
            Object[] parameters = request.getParameters();
            int num = parameters.length;
            Class[] parameterTypes = new Class[num];
            for (int i = 0; i < num; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }
            Method method = clazz.getMethod(request.getMethodName(), parameterTypes);
            result = method.invoke(clazz.newInstance(), parameters);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()) ctx.close();
    }
}

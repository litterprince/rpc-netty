package com.spring.netty.handler;

import com.spring.netty.RPC;
import com.spring.netty.util.Request;
import com.spring.netty.util.Response;
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
        String requestJson = (String) msg;
        System.out.println("receive request:" + requestJson);
        Request request = RPC.requestDecode(requestJson);
        Object result = invoke(request);
        //netty的write方法并没有直接写入通道(为避免多次唤醒多路复用选择器)
        //而是把待发送的消息放到缓冲数组中，flush方法再全部写到通道中

        //记得加分隔符 不然客户端一直不会处理
        Response response = new Response();
        response.setRequestId(request.getRequestId());
        response.setResult(result);
        String respStr = RPC.responseEncode(response);
        ByteBuf responseBuf = Unpooled.copiedBuffer(respStr.getBytes());
        ctx.writeAndFlush(responseBuf);
    }

    public static Object invoke(Request request){
        Object result = null;
        String implClassName = RPC.getServerConfig().getServerImplMap().get(request.getClassName());
        // TODO: 学习，这里的Map里面存储的impl名字而不是class对象
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

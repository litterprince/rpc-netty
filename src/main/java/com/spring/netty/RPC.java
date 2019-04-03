package com.spring.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.netty.client.ProxyObject;
import com.spring.netty.client.RPCClient;
import com.spring.netty.config.ClientConfig;
import com.spring.netty.config.ServerConfig;
import com.spring.netty.message.Request;
import com.spring.netty.message.Response;
import com.spring.netty.server.RPCServer;
import com.spring.netty.server.ServerHandler;
import com.spring.netty.util.RPCConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RPC {
    public static ApplicationContext serverContext;
    public static ApplicationContext clientContext;
    //是否有线程安全问题（结论：经过网上查找结论是线程安全）
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Object call(Class cls) {
        return ProxyObject.getProxyObject(cls);
    }

    public static void serverStart() {
        // TODO: 完成服务的上报和监控

        RPCServer.connect();
    }

    public static ServerConfig getServerConfig(){
        return serverContext.getBean(ServerConfig.class);
    }

    public static ClientConfig getClientConfig(){
        return clientContext.getBean(ClientConfig.class);
    }

    public static String requestEncode(Request request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request)+System.getProperty("line.separator");
    }

    public static Request requestDecode(String json) throws IOException {
        return objectMapper.readValue(json, Request.class);
    }

    public static String responseEncode(Response response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response)+System.getProperty("line.separator");
    }

    public static Object responseDecode(String json) throws IOException {
        return objectMapper.readValue(json, Response.class);
    }
}

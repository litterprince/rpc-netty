package com.spring.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.netty.client.ProxyObject;
import com.spring.netty.config.ClientConfig;
import com.spring.netty.config.ServerConfig;
import com.spring.netty.message.Request;
import com.spring.netty.message.Response;
import com.spring.netty.server.RPCServer;
import com.spring.netty.zk.ZKConnect;
import com.spring.netty.zk.ZKServerService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class RPC {
    public static ApplicationContext serverContext;
    public static ApplicationContext clientContext;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Object call(Class cls) {
        return ProxyObject.getProxyObject(cls);
    }

    public static void serverStart() {
        // TODO: 关键，服务的上报和监控
        ZooKeeper zk = new ZKConnect().serverConnect();
        ZKServerService serverService = new ZKServerService(zk);
        try {
            serverService.initZNode();
            serverService.createServerService();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        RPCServer.start();
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

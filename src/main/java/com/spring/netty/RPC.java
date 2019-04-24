package com.spring.netty;

import com.spring.netty.client.AsyncProxyObject;
import com.spring.netty.client.ProxyObject;
import com.spring.netty.config.ClientConfig;
import com.spring.netty.config.ServerConfig;
import com.spring.netty.promise.impl.Deferred;
import com.spring.netty.server.RPCServer;
import com.spring.netty.zk.ZKConnect;
import com.spring.netty.zk.ZKServerService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Proxy;

public class RPC {
    public static ApplicationContext serverContext;
    public static ApplicationContext clientContext;

    public static Object call(Class cls) {
        ProxyObject handler = new ProxyObject();
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, handler);
    }

    public static Object asyncCall(Class cls, Deferred promise) {
        AsyncProxyObject handler = new AsyncProxyObject(promise);
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, handler);
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

    public static ServerConfig getServerConfig() {
        return serverContext.getBean(ServerConfig.class);
    }

    public static ClientConfig getClientConfig() {
        return clientContext.getBean(ClientConfig.class);
    }
}

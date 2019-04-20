package com.spring.netty.client;

import com.spring.netty.RPC;
import com.spring.netty.message.Request;
import com.spring.netty.pool.ConnectionPool;
import com.spring.netty.promise.impl.Deferred;
import com.spring.netty.protocol.Protocol;
import com.spring.netty.util.LoadBalance;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RPCClient {
    private static RPCClient instance;
    // 异步请求
    private Map<String, Deferred> promiseMap = new ConcurrentHashMap<>();
    // 全局map 每个请求对应的锁 用于同步等待每个异步的RPC请求
    private Map<String, Request> requestLockMap = new ConcurrentHashMap<>();
    // <IP,管道>
    //private Map<String, IPChannelInfo> IPChannelInfoMap = new ConcurrentHashMap<>();
    // <IP,锁>，每个IP对应一个锁，防止重复连接一个IP多次
    //private Map<String, ReentrantReadWriteLock> connectLock = new ConcurrentHashMap<>();
    // <IP, 连接池>
    private Map<String, ConnectionPool> connectionPoolMap = new ConcurrentHashMap<>();
    // <服务名,服务信息类>
    private Map<String, ServiceInfo> serviceInfoMap = new ConcurrentHashMap<>();
    /*// <服务名,读写锁>，用于操作服务信息，更新服务名下的IP使用写锁，读取IP使用读锁
    private Map<String, ReentrantReadWriteLock> serviceLockMap = new ConcurrentHashMap<>();*/

    private LoadBalance loadBalance;

    public static RPCClient getInstance() {
        if (instance == null) {
            synchronized (RPCClient.class) {
                if (instance == null) {
                    instance = new RPCClient();
                }
            }
        }
        return instance;
    }

    private RPCClient() {
        loadBalance = RPC.getClientConfig().getLoadBalance();
    }

    // TODO: 关键，通过serviceInfo去发现服务
    public void send(Request request) {
        // 选出Ip
        try {
            String className = request.getClassName();
            String address = loadBalance.chooseAddress(className);

            // 通过连接池获取channel
            Channel channel = connectionPoolMap.get(address).getChannel();
            // send request
            /*String requestJson = null;
            try {
                requestJson = RPC.requestEncode(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert requestJson != null;
            ByteBuf byteBuffer = Unpooled.copiedBuffer(requestJson.getBytes());*/
            Protocol protocol = new Protocol();
            protocol.buildRequestProtocol(request);
            channel.writeAndFlush(protocol);
            // 使用完释放channel
            connectionPoolMap.get(address).releaseChannel(channel);

            synchronized (request) {
                request.wait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void asyncSend(Request request) {
        try {
            String className = request.getClassName();
            String address = loadBalance.chooseAddress(className);

            // 通过连接池获取channel
            Channel channel = connectionPoolMap.get(address).getChannel();
            Protocol protocol = new Protocol();
            protocol.buildRequestProtocol(request);
            channel.writeAndFlush(protocol);
            // 使用完释放channel
            connectionPoolMap.get(address).releaseChannel(channel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Deferred> getPromiseMap() {
        return promiseMap;
    }

    public void setPromiseMap(Map<String, Deferred> promiseMap) {
        this.promiseMap = promiseMap;
    }

    public Map<String, Request> getRequestLockMap() {
        return requestLockMap;
    }

    public void setRequestLockMap(Map<String, Request> requestLockMap) {
        this.requestLockMap = requestLockMap;
    }

    public Map<String, ServiceInfo> getServiceInfoMap() {
        return serviceInfoMap;
    }

    public void setServiceInfoMap(Map<String, ServiceInfo> serviceInfoMap) {
        this.serviceInfoMap = serviceInfoMap;
    }

    public Map<String, ConnectionPool> getConnectionPoolMap() {
        return connectionPoolMap;
    }

    public void setConnectionPoolMap(Map<String, ConnectionPool> connectionPoolMap) {
        this.connectionPoolMap = connectionPoolMap;
    }
}

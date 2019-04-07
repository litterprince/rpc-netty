package com.spring.netty.client;

import com.spring.netty.RPC;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.message.Request;
import com.spring.netty.pool.ConnectionPool;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.util.RPCConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RPCClient {
    private static RPCClient instance;
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
    // <服务名,读写锁>，用于操作服务信息，更新服务名下的IP使用写锁，读取IP使用读锁
    private Map<String, ReentrantReadWriteLock> serviceLockMap = new ConcurrentHashMap<>();

    private LoadBalance loadBalance;

    public static Lock lock = new ReentrantLock();
    public static Condition condition = lock.newCondition();

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
    public void send(Request request) throws ProvidersNoFoundException {
        // 选出Ip
        try {
            String className = request.getClassName();
            String address = loadBalance.chooseAddress(className);

            // 通过连接池获取channel
            Channel channel = connectionPoolMap.get(address).getChannel();
            // send request
            String requestJson = null;
            try {
                requestJson = RPC.requestEncode(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert requestJson != null;
            ByteBuf byteBuffer = Unpooled.copiedBuffer(requestJson.getBytes());
            channel.writeAndFlush(byteBuffer);
            // 使用完释放channel
            connectionPoolMap.get(address).releaseChannel(channel);

            synchronized (request) {
                // TODO: 学习，实现客户端阻塞等待
                request.wait();
            }
            System.out.println("调用" + request.getRequestId() + "接收完毕:" + request.getResult().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Request> getRequestLockMap() {
        return requestLockMap;
    }

    public void setRequestLockMap(Map<String, Request> requestLockMap) {
        this.requestLockMap = requestLockMap;
    }

    /*public Map<String, ReentrantReadWriteLock> getConnectLock() {
        return connectLock;
    }*/

    /*public void setConnectLock(Map<String, ReentrantReadWriteLock> connectLock) {
        this.connectLock = connectLock;
    }*/

    /*public Map<String, IPChannelInfo> getIPChannelInfoMap() {
        return IPChannelInfoMap;
    }*/

    /*public void setIPChannelInfoMap(Map<String, IPChannelInfo> IPChannelInfoMap) {
        this.IPChannelInfoMap = IPChannelInfoMap;
    }*/

    public Map<String, ServiceInfo> getServiceInfoMap() {
        return serviceInfoMap;
    }

    public void setServiceInfoMap(Map<String, ServiceInfo> serviceInfoMap) {
        this.serviceInfoMap = serviceInfoMap;
    }

    public Map<String, ReentrantReadWriteLock> getServiceLockMap() {
        return serviceLockMap;
    }

    public void setServiceLockMap(Map<String, ReentrantReadWriteLock> serviceLockMap) {
        this.serviceLockMap = serviceLockMap;
    }

    public Map<String, ConnectionPool> getConnectionPoolMap() {
        return connectionPoolMap;
    }

    public void setConnectionPoolMap(Map<String, ConnectionPool> connectionPoolMap) {
        this.connectionPoolMap = connectionPoolMap;
    }
}

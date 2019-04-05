package com.spring.netty.client;

import com.spring.netty.RPC;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.message.Request;
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
    // <IP地址,管道>集合
    private Map<String, IPChannelInfo> IPChannelInfoMap = new ConcurrentHashMap<>();
    // <IP地址,锁>集合，每个IP对应一个锁，防止重复连接一个IP多次
    private Map<String, ReentrantReadWriteLock> connectLock = new ConcurrentHashMap<>();
    // <服务名,服务信息类>集合
    private Map<String, ServiceInfo> serviceInfoMap = new ConcurrentHashMap<>();
    // <服务名,读写锁>集合，用于操作服务信息，更新服务名下的IP使用写锁，读取IP使用读锁
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

    public Channel connect(String ip, int port) {
        Channel channel = null;

        // 存在channel
        connectLock.get(ip).readLock().lock();
        channel = IPChannelInfoMap.get(ip).getChannel();
        if (channel != null) {
            connectLock.get(ip).readLock().unlock();
            return channel;
        }

        // 不存在channel
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        try {
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(RPCConstant.MSG_MAX_LENGTH));
                            //将接收到的对象转为字符串
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });

            final ChannelFuture f = b.connect(ip, port).sync();
            channel = f.channel();
            connectLock.get(ip).writeLock().lock();
            IPChannelInfo channelInfo = new IPChannelInfo();
            channelInfo.setGroup(group);
            channelInfo.setChannel(channel);
            IPChannelInfoMap.putIfAbsent(ip, channelInfo);
            connectLock.get(ip).writeLock().unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    // TODO: 关键，通过serviceInfo去发现服务
    private RPCClient() {
        loadBalance = RPC.getClientConfig().getLoadBalance();
    }

    public void send(Request request) throws ProvidersNoFoundException {
        // 选出Ip
        try {
            String methodName = request.getMethodName();
            String address = loadBalance.chooseAddress(methodName);
            String ip = address.split(":")[0];
            int port = Integer.parseInt(address.split(":")[1]);
            // 通过connect获得channel
            Channel channel = connect(ip, port);
            // 使用channel传递数据

            // TODO: 待续，没有阻塞等待连接完成
            /*if (ClientHandler.ctx == null) {
                lock.lock();
                System.out.println("wait connect success ...");
                condition.await();
                lock.unlock();
            }*/

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

    public Map<String, ReentrantReadWriteLock> getConnectLock() {
        return connectLock;
    }

    public void setConnectLock(Map<String, ReentrantReadWriteLock> connectLock) {
        this.connectLock = connectLock;
    }

    public Map<String, IPChannelInfo> getIPChannelInfoMap() {
        return IPChannelInfoMap;
    }

    public void setIPChannelInfoMap(Map<String, IPChannelInfo> IPChannelInfoMap) {
        this.IPChannelInfoMap = IPChannelInfoMap;
    }

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
}

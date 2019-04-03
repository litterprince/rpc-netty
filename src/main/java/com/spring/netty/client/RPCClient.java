package com.spring.netty.client;

import com.spring.netty.RPC;
import com.spring.netty.message.Request;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.util.RPCConstant;
import com.spring.netty.util.impl.RandomBalance;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
    // <IP地址,锁>集合，每个IP对应一个锁，防止重复连接一个IP多次
    private Map<String, Lock> connectLock = new ConcurrentHashMap<>();
    // <IP地址,管道>集合
    private Map<String, IPChannelInfo> IPChannelInfoMap = new ConcurrentHashMap<>();
    // <服务名,服务信息类>集合
    private Map<String, ServiceInfo> serviceInfoMap = new ConcurrentHashMap<>();
    // <服务名,读写锁>集合，用于操作服务信息，更新服务名下的IP使用写锁，读取IP使用读锁
    private Map<String, ReentrantReadWriteLock> serviceLockMap = new ConcurrentHashMap<>();

    private LoadBalance loadBalance = new RandomBalance();

    public static Lock lock = new ReentrantLock();
    public static Condition condition = lock.newCondition();
    private static EventLoopGroup group;

    private RPCClient() {
        // start client netty
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        try {
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // TODO: 学习，以换行符分包 防止念包半包 2048为最大长度 到达最大长度没出现换行符则抛出异常
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(RPCConstant.MSG_MAX_LENGTH));
                            //将接收到的对象转为字符串
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });

            final ChannelFuture f = b.connect(RPC.getClientConfig().getHost(), RPC.getClientConfig().getPort()).sync();
            // TODO: 学习，这里不能使用f.channel().closeFuture().sync()方法，该方法会使线程阻塞在这，等待管道关闭
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RPCClient connect() {
        if (instance == null) {
            synchronized (RPCClient.class) {
                if (instance == null) {
                    instance = new RPCClient();
                }
            }
        }
        return instance;
    }

    public static RPCClient getInstance(){
        return connect();
    }

    public static void send(Request request) {
        try {
            // TODO: 学习，掌握这种阻塞方式
            if (ClientHandler.ctx == null) {
                lock.lock();
                System.out.println("wait connect success ...");
                condition.await();
                lock.unlock();
            }

            // send request
            String requestJson = null;
            try {
                requestJson = RPC.requestEncode(request);
            } catch (Exception e) {
                e.printStackTrace();
            }

            assert requestJson != null;
            ByteBuf byteBuffer = Unpooled.copiedBuffer(requestJson.getBytes());
            ClientHandler.ctx.writeAndFlush(byteBuffer);
            // TODO: 测试，线程安全
            Object[] parameters = request.getParameters();
            String msg = parameters.length > 1 ? parameters[0].toString() : "";
            System.out.println("调用" + request.getRequestId() + "已发送:" + msg);

            synchronized (request) {
                // TODO: 学习，实现客户端阻塞等待
                request.wait();
            }
            System.out.println("调用" + request.getRequestId() + "接收完毕:"+request.getResult().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (!group.isShutdown()) {
            group.shutdownGracefully();
        }
    }

    public Map<String, Request> getRequestLockMap() {
        return requestLockMap;
    }

    public void setRequestLockMap(Map<String, Request> requestLockMap) {
        this.requestLockMap = requestLockMap;
    }

    public Map<String, Lock> getConnectLock() {
        return connectLock;
    }

    public void setConnectLock(Map<String, Lock> connectLock) {
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

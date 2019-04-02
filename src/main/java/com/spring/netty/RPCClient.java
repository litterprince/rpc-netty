package com.spring.netty;

import com.spring.netty.handler.ClientHandler;
import com.spring.netty.util.ConstantUtil;
import com.spring.netty.util.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RPCClient {
    //全局map 每个请求对应的锁 用于同步等待每个异步的RPC请求
    public static ConcurrentHashMap<String, Request> requestLockMap = new ConcurrentHashMap<>();
    private static RPCClient instance;

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
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(ConstantUtil.MSG_MAX_LENGTH));
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
}

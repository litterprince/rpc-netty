package com.spring.netty.server;

import com.spring.netty.RPC;
import com.spring.netty.protocol.Decoder;
import com.spring.netty.protocol.Encoder;
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

public class RPCServer {
    public static void start(){
        // start server netty
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 输入连接指示（对连接的请求）的最大队列长度被设置为 backlog 参数。如果队列满时收到连接指示，则拒绝该连接
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            /*socketChannel.pipeline().addLast(new LineBasedFrameDecoder(RPCConstant.MSG_MAX_LENGTH));
                            // 将接收到的对象转为字符串
                            socketChannel.pipeline().addLast(new StringDecoder());*/
                            socketChannel.pipeline().addLast(new Decoder(2048));
                            socketChannel.pipeline().addLast(new Encoder());
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });

            String[] serverHost = RPC.getServerConfig().getServerHost().split(":");
            ChannelFuture future = b.bind(serverHost[0], Integer.parseInt(serverHost[1])).sync();
            System.out.println("server start on:"+RPC.getServerConfig().getServerHost());
            // 同步等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

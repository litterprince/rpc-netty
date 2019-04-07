package com.spring.netty.pool;

import com.spring.netty.client.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectFactory extends BasePooledObjectFactory<Channel> {
    private String ip;
    private Integer port;
    //netty线程组 同一个服务的连接池内各个连接共用
    private EventLoopGroup group = new NioEventLoopGroup();

    public ConnectFactory(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    @Override
    public Channel create() throws Exception {
        //启动辅助类 用于配置各种参数
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //以换行符分包 防止粘包半包 2048为最大长度 到达最大长度没出现换行符则抛出异常
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(2048));
                        //将接收到的对象转为字符串
                        socketChannel.pipeline().addLast(new StringDecoder());
                        //添加相应回调处理和编解码器
                        socketChannel.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture f = b.connect(ip, port).sync();
        System.out.println("pool create channel " + ip + ":" + port);
        return f.channel();
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        System.out.println("destroy channel "+ip+":"+port);
        //销毁channel时释放资源
        p.getObject().close();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }
}

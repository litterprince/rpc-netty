package com.spring.netty.pool;

import com.spring.netty.RPC;
import io.netty.channel.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool {
    private GenericObjectPool pool;
    private String address;
    private AtomicInteger count = new AtomicInteger(0);

    public ConnectionPool(String ip, Integer port) {
        ConnectFactory factory = new ConnectFactory(ip, port);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        //最大空闲连接数
        config.setMaxIdle(RPC.getClientConfig().getPoolMaxIdle());
        //最大连接数
        config.setMaxTotal(RPC.getClientConfig().getPoolMaxTotal());
        pool = new GenericObjectPool<Channel>(factory, config);
        address = ip + ":" + port;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                destroyChannel();
            }
        }));
    }

    public Channel getChannel() throws Exception {
        count.incrementAndGet();
        return (Channel) pool.borrowObject();
    }

    public void releaseChannel(Channel channel) {
        count.decrementAndGet();
        pool.returnObject(channel);
    }

    public void destroyChannel() {
        ((ConnectFactory) pool.getFactory()).getGroup().shutdownGracefully();
        pool.close();
    }

    public int getCount() {
        return count.get();
    }
}

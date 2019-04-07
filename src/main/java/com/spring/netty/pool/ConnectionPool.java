package com.spring.netty.pool;

import com.spring.netty.RPC;
import com.spring.netty.client.RPCClient;
import io.netty.channel.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ConnectionPool {
    private GenericObjectPool pool;
    private String fullIp;

    public ConnectionPool(String ip, Integer port) {
        ConnectFactory factory = new ConnectFactory(ip, port);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        //最大空闲连接数
        config.setMaxIdle(RPC.getClientConfig().getPoolMaxIdle());
        //最大连接数
        config.setMaxTotal(RPC.getClientConfig().getPoolMaxTotal());
        pool = new GenericObjectPool<Channel>(factory, config);
        fullIp = ip + ":" + port;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                destroyChannel();
            }
        }));
    }

    public Channel getChannel() throws Exception {
        return (Channel) pool.borrowObject();
    }

    public void releaseChannel(Channel channel){
        pool.returnObject(channel);
    }

    public void destroyChannel(){
        ((ConnectFactory)pool.getFactory()).getGroup().shutdownGracefully();
        pool.close();
        RPCClient.getInstance().getConnectionPoolMap().remove(fullIp);
    }
}

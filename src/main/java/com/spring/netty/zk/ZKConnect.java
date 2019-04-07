package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.util.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

// TODO: 完善，后续支持zookeeper集群
public class ZKConnect {
    private ZooKeeper zooKeeper = null;

    private ZooKeeper connect(String host){
        CountDownLatch latch = new CountDownLatch(1);
        try {
            zooKeeper = new ZooKeeper(host, Constant.ZK_TIME_OUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    public ZooKeeper serverConnect() {
        String host = RPC.getServerConfig().getZookeeperHost();
        return connect(host);
    }

    public ZooKeeper clientConnect(){
        String host = RPC.getClientConfig().getZookeeperHost();
        return connect(host);
    }
}

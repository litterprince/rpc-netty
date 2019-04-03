package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.util.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

// TODO: 待续，完成zk的连接
public class ZKConnect implements Watcher {
    public ZooKeeper serverConnect() {
        String address = RPC.getServerConfig().getZookeeperHost();
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(address, Constant.ZK_TIME_OUT, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}

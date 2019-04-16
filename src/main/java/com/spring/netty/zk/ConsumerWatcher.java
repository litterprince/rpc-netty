package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.util.LoadBalance;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class ConsumerWatcher implements Watcher {
    private ZooKeeper zooKeeper;

    public ConsumerWatcher(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String path = watchedEvent.getPath();
        String[] pathArr = path.split("/");
        String serviceName = pathArr[2];//第三个部分则为服务名
        try {
            List<String> children = zooKeeper.getChildren(path, this);
            LoadBalance loadBalance = RPC.getClientConfig().getLoadBalance();
            loadBalance.balance(zooKeeper, serviceName, children, ZNodeType.CONSUMER);
        } catch (KeeperException | InterruptedException | ProvidersNoFoundException e) {
            e.printStackTrace();
        }
    }
}

package com.spring.netty.zk;

import com.spring.netty.RPC;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * 服务提供者ip监控
 */
public class IPWatcher implements Watcher {
    private ZooKeeper zooKeeper;

    public IPWatcher(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // TODO: 关键，ip节点变化后的处理
        try {
            String path = watchedEvent.getPath();
            String serviceName = path.split("/")[3];
            // 新增情况处理
            List<String> newAddress = zooKeeper.getChildren(path, this);
            RPC.getClientConfig().getLoadBalance().balance(zooKeeper, serviceName, newAddress, ZNodeType.CONSUMER);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package com.spring.netty.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

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
        // TODO: 待续，监控到服务提供者IP变化后进行重置操作，重新获取可用IP 写入serviceInfo 并再次注册watcher

    }
}

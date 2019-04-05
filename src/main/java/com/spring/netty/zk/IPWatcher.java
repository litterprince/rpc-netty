package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.client.IPChannelInfo;
import com.spring.netty.client.RPCClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        // TODO: 完善，只是处理了添加ip的情况，删除ip的情况没有处理
        String path = watchedEvent.getPath();
        String serviceName = path.split("/")[3];
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().lock();
        try {
            List<String> ips = zooKeeper.getChildren(path, this);
            for (String ip : ips){
                RPCClient.getInstance().getIPChannelInfoMap().putIfAbsent(ip, new IPChannelInfo());
                RPCClient.getInstance().getConnectLock().putIfAbsent(ip, new ReentrantReadWriteLock());
            }
            RPCClient.getInstance().getServiceInfoMap().get(serviceName).setServiceIPSet(ips);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().unlock();
    }
}

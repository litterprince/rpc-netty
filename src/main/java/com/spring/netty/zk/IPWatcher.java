package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.client.IPChannelInfo;
import com.spring.netty.client.RPCClient;
import com.spring.netty.pool.ConnectionPool;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Set;
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
        // TODO: 关键，ip节点变化后的处理
        String path = watchedEvent.getPath();
        String serviceName = path.split("/")[3];
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().lock();
        try {
            // 新增情况处理
            List<String> newAddress = zooKeeper.getChildren(path, this);
            for (String address : newAddress){
                String ip = address.split(":")[0];
                int port = Integer.parseInt(address.split(":")[1]);
                RPCClient.getInstance().getConnectionPoolMap().putIfAbsent(address, new ConnectionPool(ip, port));
            }
            RPCClient.getInstance().getServiceInfoMap().get(serviceName).setServiceIPSet(newAddress);
            // 删除情况处理
            Set<String> oldAddress = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
            oldAddress.removeAll(newAddress);
            for (String address : oldAddress){
                RPCClient.getInstance().getConnectionPoolMap().get(address).destroyChannel();
                RPCClient.getInstance().getConnectionPoolMap().remove(address);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().unlock();
    }
}

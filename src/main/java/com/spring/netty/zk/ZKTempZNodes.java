package com.spring.netty.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ZKTempZNodes {
    private ZooKeeper zooKeeper;

    public ZKTempZNodes(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    // 创建临时节点
    public void createTempZNode(String s, Object o) {
        try {
            if(!isNodeExist(s)) {
                zooKeeper.create(s, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 创建持久节点
    public void createSimpleZNode(String s, Object o) {
        try {
            if(!isNodeExist(s)) {
                zooKeeper.create(s, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPathChildren(String s, IPWatcher ipWatcher) {
        try {
            return zooKeeper.getChildren(s, ipWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isNodeExist(String s){
        Stat stat = null;
        try {
            stat = zooKeeper.exists(s, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return stat != null;
    }
}

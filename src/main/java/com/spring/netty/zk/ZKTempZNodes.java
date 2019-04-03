package com.spring.netty.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

// TODO: 待续，完成对zk操作的封装
public class ZKTempZNodes {
    private ZooKeeper zooKeeper;

    public ZKTempZNodes(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    // 创建临时节点
    public void createTempZNode(String s, Object o) {
        try {
            zooKeeper.create(s, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 创建持久节点
    public void createSimpleZNode(String s, Object o) {
        try {
            zooKeeper.create(s, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
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
}

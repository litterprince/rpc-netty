package com.spring.netty.util.impl;

import com.spring.netty.client.RPCClient;
import com.spring.netty.client.ServiceInfo;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.pool.ConnectionPool;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.ZNodeType;
import org.apache.zookeeper.ZooKeeper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractRandom implements LoadBalance {
    @Override
    abstract public String chooseAddress(String serviceName) throws ProvidersNoFoundException;

    @Override
    public void balance(ZooKeeper zooKeeper, String serviceName, List<String> zNodes, ZNodeType type) {
        // 初始化
        if (RPCClient.getInstance().getServiceInfoMap().get(serviceName) == null) {
            ServiceInfo serviceInfo = new ServiceInfo();
            RPCClient.getInstance().getServiceInfoMap().putIfAbsent(serviceName, serviceInfo);
        }
        // provider ip节点变化
        Set<String> serviceIPSet = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
        Set<String> oldAddress = new HashSet<>(serviceIPSet);
        RPCClient.getInstance().getServiceInfoMap().get(serviceName).setServiceIPSet(zNodes);

        // 新增ip的情况
        for (String address : zNodes) {
            String ip = address.split(":")[0];
            int port = Integer.parseInt(address.split(":")[1]);
            RPCClient.getInstance().getConnectionPoolMap().putIfAbsent(address, new ConnectionPool(ip, port));
        }

        // 删除ip的情况
        oldAddress.removeAll(zNodes);
        for (String address : oldAddress) {
            RPCClient.getInstance().getConnectionPoolMap().get(address).destroyChannel();
            RPCClient.getInstance().getConnectionPoolMap().remove(address);
        }
    }
}

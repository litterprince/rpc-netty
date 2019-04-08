package com.spring.netty.util.impl;

import com.spring.netty.client.RPCClient;
import com.spring.netty.client.ServiceInfo;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.pool.ConnectionPool;
import com.spring.netty.util.Constant;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.IPWatcher;
import com.spring.netty.zk.ZKTempZNodes;
import com.spring.netty.zk.ZNodeType;
import org.apache.zookeeper.ZooKeeper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractRandom implements LoadBalance {
    @Override
    abstract public String chooseAddress(String serviceName) throws ProvidersNoFoundException;

    @Override
    public void balance(ZooKeeper zooKeeper, String serviceName, List<String> zNodes, ZNodeType type) throws ProvidersNoFoundException {
        // 初始化
        if (RPCClient.getInstance().getServiceInfoMap().get(serviceName) == null) {
            initService(serviceName);
        }

        // provider ip节点变化
        Set<String> serviceIPSet = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
        Set<String> oldAddress = new HashSet<>(serviceIPSet);
        RPCClient.getInstance().getServiceInfoMap().get(serviceName).setServiceIPSet(zNodes);

        // 新增ip的情况
        for (String address : zNodes) {
            addNewAddress(serviceName, address);
        }

        // 删除ip的情况
        oldAddress.removeAll(zNodes);
        for (String address : oldAddress) {
            removeAddress(serviceName, address);
        }
    }

    @Override
    public void balanceAll(ZooKeeper zooKeeper, Set<String> serviceInterface, ZNodeType type) throws ProvidersNoFoundException {
        ZKTempZNodes zkTempZnodes = new ZKTempZNodes(zooKeeper);
        for (String serviceName : serviceInterface) {
            IPWatcher ipWatcher = new IPWatcher(zooKeeper);
            String path = Constant.ROOT_PATH + Constant.SERVICE_PATH + "/" +
                    serviceName + Constant.PROVIDERS_PATH;
            List<String> addresses = zkTempZnodes.getPathChildren(path, ipWatcher);
            balance(zooKeeper, serviceName, addresses, ZNodeType.CONSUMER);
        }
    }

    protected void initService(String serviceName) throws ProvidersNoFoundException {
        ServiceInfo serviceInfo = new ServiceInfo();
        RPCClient.getInstance().getServiceInfoMap().putIfAbsent(serviceName, serviceInfo);
    }

    protected void addNewAddress(String serviceName, String address) {
        String ip = address.split(":")[0];
        int port = Integer.parseInt(address.split(":")[1]);
        RPCClient.getInstance().getConnectionPoolMap().putIfAbsent(address, new ConnectionPool(ip, port));
    }

    protected void removeAddress(String serviceName, String address) {
        RPCClient.getInstance().getConnectionPoolMap().get(address).destroyChannel();
        RPCClient.getInstance().getConnectionPoolMap().remove(address);
    }
}

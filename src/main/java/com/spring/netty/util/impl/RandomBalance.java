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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RandomBalance implements LoadBalance {
    @Override
    public String chooseAddress(String serviceName) throws ProvidersNoFoundException {
        RPCClient instance = RPCClient.getInstance();
        Set<String> ipSet;
        int ipNum;
        instance.getServiceLockMap().get(serviceName).readLock().lock();
        ipSet = instance.getServiceInfoMap().get(serviceName).getServiceIPSet();
        ipNum = ipSet.size();
        instance.getServiceLockMap().get(serviceName).readLock().unlock();

        //生成[0,num)区间的整数：
        Random random = new Random();
        int index = random.nextInt(ipNum);
        int count = 0;
        for (String ip : ipSet) {
            if (count == index) {
                //返回随机生成的索引位置ip
                return ip;
            }
            count++;
        }
        return null;
    }

    @Override
    public void balance(ZooKeeper zooKeeper, String serviceName, List<String> zNodes, ZNodeType type) {
        RPCClient.getInstance().getServiceLockMap().putIfAbsent(serviceName, new ReentrantReadWriteLock());
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().lock();

        Set<String> oldAddress = null;
        if(RPCClient.getInstance().getServiceInfoMap().get(serviceName) == null) { // 初始化
            ServiceInfo serviceInfo = new ServiceInfo();
            RPCClient.getInstance().getServiceInfoMap().putIfAbsent(serviceName, serviceInfo);
            RPCClient.getInstance().getServiceInfoMap().get(serviceName).setServiceIPSet(zNodes);
        } else { // provider ip节点变化
            Set<String> serviceIPSet = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
            oldAddress = new HashSet<>(serviceIPSet);
        }

        // 新增ip的情况
        for (String address : zNodes) {
            String ip = address.split(":")[0];
            int port = Integer.parseInt(address.split(":")[1]);
            RPCClient.getInstance().getConnectionPoolMap().putIfAbsent(address, new ConnectionPool(ip, port));
        }

        // 删除ip的情况
        if(oldAddress == null){
            RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().unlock();
            return;
        }
        oldAddress.removeAll(zNodes);
        for (String address : oldAddress) {
            RPCClient.getInstance().getConnectionPoolMap().get(address).destroyChannel();
            RPCClient.getInstance().getConnectionPoolMap().remove(address);
        }
        RPCClient.getInstance().getServiceLockMap().get(serviceName).writeLock().unlock();
    }
}

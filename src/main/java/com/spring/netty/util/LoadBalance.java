package com.spring.netty.util;

import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.zk.ZNodeType;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Set;

public interface LoadBalance {
    String chooseAddress(String serviceName) throws ProvidersNoFoundException;

    void balance(ZooKeeper zooKeeper, String serviceName, List<String> zNodes, ZNodeType type) throws ProvidersNoFoundException;

    void balanceAll(ZooKeeper zooKeeper, Set<String> serviceInterface, ZNodeType type) throws ProvidersNoFoundException;
}

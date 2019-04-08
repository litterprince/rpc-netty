package com.spring.netty.util.impl;

import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.ZNodeType;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class MinConnectRandom implements LoadBalance {
    @Override
    public String chooseAddress(String serviceName) throws ProvidersNoFoundException {
        return null;
    }

    // TODO: 待续，完成balance方法
    @Override
    public void balance(ZooKeeper zooKeeper, String serviceName, List<String> zNodes, ZNodeType type) {

    }
}

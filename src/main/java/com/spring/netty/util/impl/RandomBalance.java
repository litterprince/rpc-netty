package com.spring.netty.util.impl;

import com.spring.netty.client.RPCClient;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.util.LoadBalance;

import java.util.Random;
import java.util.Set;

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
}

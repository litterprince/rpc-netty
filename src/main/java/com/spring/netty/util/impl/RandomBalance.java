package com.spring.netty.util.impl;

import com.spring.netty.client.RPCClient;
import com.spring.netty.exception.ProvidersNoFoundException;

import java.util.Random;
import java.util.Set;

public class RandomBalance extends AbstractRandom {
    @Override
    public String chooseAddress(String serviceName) throws ProvidersNoFoundException {
        int ipNum;
        Set<String> addresses = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
        ipNum = addresses.size();

        //生成[0,num)区间的整数：
        Random random = new Random();
        int index = random.nextInt(ipNum);
        int count = 0;
        for (String address : addresses) {
            if (count == index) {
                //返回随机生成的索引位置ip
                return address;
            }
            count++;
        }
        return null;
    }
}

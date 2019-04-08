package com.spring.netty.util.impl;

import com.spring.netty.client.RPCClient;
import com.spring.netty.exception.ProvidersNoFoundException;

import java.util.Set;

public class MinConnectRandom extends AbstractRandom {
    @Override
    public String chooseAddress(String serviceName) throws ProvidersNoFoundException {
        Set<String> addresses = RPCClient.getInstance().getServiceInfoMap().get(serviceName).getServiceIPSet();
        String minAddress = "";
        int minCount = -1;
        for (String address : addresses){
            int count = RPCClient.getInstance().getConnectionPoolMap().get(address).getCount();
            if(count < minCount){
                minAddress = address;
                minCount = count;
            }
        }
        return minAddress;
    }
}

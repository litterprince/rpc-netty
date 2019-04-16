package com.spring.netty.client;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceInfo {
    //用于轮询负载均衡策略
    private AtomicInteger index = new AtomicInteger(0);

    //这个服务所连接的提供者IP Set 只能由负载均衡类操作
    private Set<String> serviceIPSet = Sets.newConcurrentHashSet();

    public void setServiceIPSet(List<String> newIPSet) {
        Set<String> set = new HashSet<>(newIPSet);
        this.serviceIPSet.clear();
        this.serviceIPSet.addAll(set);
    }

    public Set<String> getServiceIPSet() {
        return serviceIPSet;
    }

    public int getConnectIPSetCount(){
        return serviceIPSet.size();
    }

    public void addConnectIP(String IP) {
        serviceIPSet.add(IP);
    }

    public void removeConnectIP(String IP){
        serviceIPSet.remove(IP);
    }
}

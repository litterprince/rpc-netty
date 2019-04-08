package com.spring.netty.util.impl;

import com.spring.netty.RPC;
import com.spring.netty.exception.ProvidersNoFoundException;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 一致性hash
 */
public class ConsistentHashing extends AbstractRandom {
    //每个服务用红黑树维护有序的一致性Hash环
    private Map<String, SortedMap<Integer, String>> sortedServersMap = new ConcurrentHashMap<>();
    private Map<String, ReentrantReadWriteLock> sortLock = new ConcurrentHashMap<>();

    //虚拟节点数 默认16个虚拟节点
    private final Integer virtualNodeNums = 16;

    //FNV1_32_HASH Hash算法
    private Integer getHash(String address) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < address.length(); i++) {
            hash = (hash ^ address.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    private boolean contains(String serviceName, String address) {
        boolean f = false;
        try {
            sortLock.get(serviceName).readLock().lock();
            f = sortedServersMap.get(serviceName).containsKey(getHash(address + "-" + 0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sortLock.get(serviceName).readLock().unlock();
        }
        return f;
    }

    private void put(String serviceName, String address) {
        try {
            sortLock.get(serviceName).writeLock().lock();
            sortedServersMap.get(serviceName).put(getHash(address), address);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sortLock.get(serviceName).writeLock().unlock();
        }
    }

    private void remove(String serviceName, String address) {
        try {
            sortLock.get(serviceName).writeLock().lock();
            sortedServersMap.get(serviceName).remove(getHash(address));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sortLock.get(serviceName).writeLock().unlock();
        }
    }

    private void putVirtualNode(String serviceName, String address) {
        if (!contains(serviceName, address)) {
            for (int i = 0; i < virtualNodeNums; i++) {
                String virtualIP = address + "-" + i;
                put(serviceName, virtualIP);
            }
        }
    }

    private void removeVirtualNode(String serviceName, String address) {
        for (int i = 0; i < virtualNodeNums; i++) {
            String virtualIP = address + "-" + i;
            remove(serviceName, virtualIP);
        }
    }

    @Override
    public String chooseAddress(String serviceName) throws ProvidersNoFoundException {
        SortedMap<Integer, String> serverRBTree = sortedServersMap.get(serviceName);
        Integer consumerHash = getHash(RPC.getClientConfig().getHost());
        Integer key = consumerHash;
        if (!serverRBTree.containsKey(consumerHash)) {
            // 得到大于该Hash值的所有Map
            SortedMap<Integer, String> tailMap = serverRBTree.tailMap(consumerHash);
            if (tailMap.isEmpty()) {
                //如果没有比它大的 就取第一个
                key = serverRBTree.firstKey();
            } else {
                //取最接近的一个
                key = tailMap.firstKey();
            }
        }
        String ipNode = serverRBTree.get(key);
        String[] realIP = ipNode.split("-");
        return realIP[0];
    }

    @Override
    public void initService(String serviceName) throws ProvidersNoFoundException {
        sortedServersMap.putIfAbsent(serviceName, new TreeMap<>());
        sortLock.putIfAbsent(serviceName, new ReentrantReadWriteLock());
        super.initService(serviceName);
    }

    @Override
    protected void addNewAddress(String serviceName, String address) {
        super.addNewAddress(serviceName, address);
        putVirtualNode(serviceName, address);
    }

    @Override
    protected void removeAddress(String serviceName, String address) {
        super.removeAddress(serviceName, address);
        removeVirtualNode(serviceName, address);
    }
}

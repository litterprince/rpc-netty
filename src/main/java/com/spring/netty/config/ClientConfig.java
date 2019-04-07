package com.spring.netty.config;

import com.spring.netty.RPC;
import com.spring.netty.client.RPCClient;
import com.spring.netty.client.ServiceInfo;
import com.spring.netty.pool.ConnectionPool;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.ZKConnect;
import com.spring.netty.zk.ZKServerService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientConfig implements ApplicationContextAware {
    @Deprecated
    private String host;
    private int port;
    private String zookeeperHost;
    private Set<String> serviceInterface;
    private LoadBalance loadBalance;
    private int poolMaxIdle;
    private int poolMaxTotal;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZookeeperHost() {
        return zookeeperHost;
    }

    public void setZookeeperHost(String zookeeperHost) {
        this.zookeeperHost = zookeeperHost;
    }

    public Set<String> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Set<String> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    public void setPoolMaxIdle(int poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }

    public int getPoolMaxTotal() {
        return poolMaxTotal;
    }

    public void setPoolMaxTotal(int poolMaxTotal) {
        this.poolMaxTotal = poolMaxTotal;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RPC.clientContext = applicationContext;

        // TODO: 关键，RPCClient里服务信息的初始化
        ZooKeeper zk = new ZKConnect().clientConnect();
        ZKServerService zkServerService = new ZKServerService(zk);
        try {
            for (String serviceName : serviceInterface) {
                List<String> addresses = zkServerService.getAllServiceAddress(serviceName);
                for (String address : addresses) {
                    String ip = address.split(":")[0];
                    int port = Integer.parseInt(address.split(":")[1]);
                    RPCClient.getInstance().getConnectionPoolMap().putIfAbsent(address, new ConnectionPool(ip, port));
                }
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setServiceIPSet(addresses);
                RPCClient.getInstance().getServiceInfoMap().putIfAbsent(serviceName, serviceInfo);
                RPCClient.getInstance().getServiceLockMap().putIfAbsent(serviceName, new ReentrantReadWriteLock());
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}

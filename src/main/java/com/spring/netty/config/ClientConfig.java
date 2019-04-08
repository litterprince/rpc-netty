package com.spring.netty.config;

import com.spring.netty.RPC;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.util.Constant;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.IPWatcher;
import com.spring.netty.zk.ZKConnect;
import com.spring.netty.zk.ZKTempZNodes;
import com.spring.netty.zk.ZNodeType;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Set;

public class ClientConfig implements ApplicationContextAware {
    private String host;
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
        ZooKeeper zooKeeper = new ZKConnect().clientConnect();
        try {
            loadBalance.balanceAll(zooKeeper, serviceInterface, ZNodeType.CONSUMER);
        } catch (ProvidersNoFoundException e) {
            e.printStackTrace();
        }
    }
}

package com.spring.netty.config;

import com.spring.netty.RPC;
import com.spring.netty.client.IPChannelInfo;
import com.spring.netty.client.RPCClient;
import com.spring.netty.client.ServiceInfo;
import com.spring.netty.exception.ProvidersNoFoundException;
import com.spring.netty.util.Constant;
import com.spring.netty.util.LoadBalance;
import com.spring.netty.zk.ZKConnect;
import com.spring.netty.zk.ZKServerService;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientConfig implements ApplicationContextAware {
    @Deprecated
    private String host;
    private int port;
    private String zookeeperHost;
    private Set<String> serviceInterface;
    private LoadBalance loadBalance;

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RPC.clientContext = applicationContext;

        // TODO: 关键，RPCClient里服务信息的初始化
        ZooKeeper zk = new ZKConnect().clientConnect();
        ZKServerService zkServerService = new ZKServerService(zk);
        try {
            for (String serviceName : serviceInterface) {
                List<String> ips = zkServerService.getAllServiceIP(serviceName);
                for (String ip : ips) {
                    RPCClient.getInstance().getIPChannelInfoMap().putIfAbsent(ip, new IPChannelInfo());
                    RPCClient.getInstance().getConnectLock().put(ip, new ReentrantReadWriteLock());
                }
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setServiceIPSet(ips);
                RPCClient.getInstance().getServiceInfoMap().putIfAbsent(serviceName, serviceInfo);
                RPCClient.getInstance().getServiceLockMap().putIfAbsent(serviceName, new ReentrantReadWriteLock());
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}

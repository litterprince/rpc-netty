package com.spring.netty.zk;

import com.spring.netty.RPC;
import com.spring.netty.util.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Map;

// TODO: 学习，通过zk完成服务的上报、监控
public class ZKServerService {
    private ZooKeeper zooKeeper;

    public ZKServerService(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    //初始化根节点及服务提供者节点 均为持久节点
    public void initZNode() throws KeeperException {
        ZKTempZNodes zkTempZnodes = new ZKTempZNodes(zooKeeper);
        // 创建根节点 /center
        StringBuilder pathBuilder = new StringBuilder(Constant.ROOT_PATH);
        zkTempZnodes.createSimpleZNode(pathBuilder.toString(), null);
        // 创建服务总节点 /center/services
        pathBuilder.append(Constant.SERVICE_PATH);
        zkTempZnodes.createSimpleZNode(pathBuilder.toString(), null);
        Map<String, String> serverImplMap = RPC.getServerConfig().getServerImplMap();
        for (Map.Entry<String, String> entry : serverImplMap.entrySet()) {
            // 创建服务节点 /center/services/methodName/
            StringBuilder serviceBuilder = new StringBuilder(pathBuilder.toString());
            serviceBuilder.append("/");
            serviceBuilder.append(entry.getKey());
            zkTempZnodes.createSimpleZNode(serviceBuilder.toString(), null);
            // 创建providers节点: /center/services/methodName/providers
            serviceBuilder.append(Constant.PROVIDERS_PATH);
            zkTempZnodes.createSimpleZNode(serviceBuilder.toString(), null);
        }
    }

    //生成所有注册的服务zNode
    public void createServerService() throws InterruptedException {
        ZKTempZNodes zkTempZnodes = new ZKTempZNodes(zooKeeper);
        Map<String, String> serviceMap = RPC.getServerConfig().getServerImplMap();
        String ip = RPC.getServerConfig().getServerHost();
        for (Map.Entry<String, String> entry : serviceMap.entrySet()) {
            // 创建具体provider节点 /center/services/methodName/providers/ip:port
            zkTempZnodes.createTempZNode(Constant.ROOT_PATH + Constant.SERVICE_PATH + "/" + entry.getKey() + Constant.PROVIDERS_PATH + "/" + ip, null);
        }
    }

    //获得这个服务所有的提供者 包含监听注册
    public List<String> getAllServiceIP(String serviceName) throws KeeperException {
        ZKTempZNodes zkTempZnodes = new ZKTempZNodes(zooKeeper);
        IPWatcher ipWatcher = new IPWatcher(zooKeeper);
        return zkTempZnodes.getPathChildren(Constant.ROOT_PATH + Constant.SERVICE_PATH + "/" + serviceName + Constant.PROVIDERS_PATH, ipWatcher);
    }
}

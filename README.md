### 版本 
- master

### 技术选型
- java框架：Spring
- 网络通信：Netty
- 序列化：jackson
- 注册中心：单机zookeeper
- 负载均衡策略：随机

### 新特性
- channel池化
- 增加最小连接和一致性hash均衡策略

### 待完成
- 响应超时
- 异步RPC
- 自定义协议
- 链路追踪
- zk断线重连

### 参考
- https://blog.csdn.net/we_phone/article/details/79053472
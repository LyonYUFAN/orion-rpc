package com.jiashi.rpc.core.registry.zk;

import com.jiashi.rpc.core.enumeration.RpcErrorMessageEnum;
import com.jiashi.rpc.core.exception.RpcException;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 基于 ZooKeeper的服务注册实现类
 */
@Component
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {

        // 1. 获取ZK路径
        // 根节点是/orion-rpc(在CuratorUtils里的namespace设置了)
        // 这里的path最终会是:/com.jiashi.UserService/127.0.0.1:8080
        String servicePath = "/" + serviceName + "/" + getServiceAddress(inetSocketAddress);

        CuratorFramework client = CuratorUtils.getCuratorClient();
        try{
            if (client.checkExists().forPath(servicePath) != null) {
                log.info("节点已存在，无需重复注册: {}", servicePath);
                // 可选：如果你想强制更新，可以先 delete 再 create
                // curatorFramework.delete().forPath(servicePath);
            } else {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL) // 一定要是临时节点
                        .forPath(servicePath);
                log.info("服务注册成功: {}", servicePath);
            }
        } catch (Exception e) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_REGISTER_FAILED,servicePath);
        }
    }

    /**
     * 辅助方法：将InetSocketAddress转为字符串"ip:port"
     * 例如：127.0.0.1:8080
     */
    private String getServiceAddress(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }
}


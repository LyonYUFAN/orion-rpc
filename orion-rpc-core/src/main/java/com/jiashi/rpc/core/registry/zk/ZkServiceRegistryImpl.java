package com.jiashi.rpc.core.registry.zk;

import com.jiashi.rpc.core.enumeration.RpcErrorMessageEnum;
import com.jiashi.rpc.core.exception.RpcException;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

/**
 * 基于 ZooKeeper的服务注册实现类
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {

        // 1. 获取ZK路径
        // 根节点是/orion-rpc(在CuratorUtils里的namespace设置了)
        // 这里的path最终会是:/com.jiashi.UserService/127.0.0.1:8080
        String servicePath = "/" + serviceName + "/" + getServiceAddress(inetSocketAddress);

        CuratorFramework client = CuratorUtils.getCuratorClient();
        try{
            client.create()
                    .creatingParentsIfNeeded() // 如果父节点(/com.jiashi.UserService)不存在，自动创建
                    .withMode(CreateMode.EPHEMERAL) // 核心:创建临时节点
                    .forPath(servicePath);
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


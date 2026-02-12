package com.jiashi.rpc.core.registry.zk;

import com.jiashi.rpc.core.enumeration.RpcErrorMessageEnum;
import com.jiashi.rpc.core.exception.RpcException;
import com.jiashi.rpc.core.registry.ServiceDiscovery;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(String serviceName) {

        CuratorFramework client = CuratorUtils.getCuratorClient();
        String servicePath = "/" + serviceName;
        try{
            List<String> serviceUrlList = client.getChildren().forPath(servicePath);
            if(serviceUrlList.isEmpty() || serviceUrlList == null){
                throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND,servicePath);
            }

            // 负载均衡
            // TODO: 这里目前直接取第一个地址 (serviceUrlList.get(0))。
            String targetServiceUrl = serviceUrlList.get(0);

            // 解析地址字符串 (例如 "127.0.0.1:8080") -> InetSocketAddress
            String[] socketAddressArray = targetServiceUrl.split(":");
            String host = socketAddressArray[0];
            int port = Integer.parseInt(socketAddressArray[1]);

            return new InetSocketAddress(host, port);
        }catch (Exception e){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND, servicePath);
        }
    }
}


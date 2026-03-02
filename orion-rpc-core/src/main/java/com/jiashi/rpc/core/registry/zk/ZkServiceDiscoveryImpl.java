package com.jiashi.rpc.core.registry.zk;

import com.jiashi.rpc.core.enumeration.RpcErrorMessageEnum;
import com.jiashi.rpc.core.exception.RpcException;
import com.jiashi.rpc.core.registry.ServiceDiscovery;
import com.jiashi.rpc.core.registry.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public List<ServiceInstance> lookupService(String serviceName) {

        CuratorFramework client = CuratorUtils.getCuratorClient();
        String servicePath = "/" + serviceName;
        try{
            List<String> serviceUrlList = client.getChildren().forPath(servicePath);
            if(serviceUrlList.isEmpty() || serviceUrlList == null){
                throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND,servicePath);
            }

            // 负载均衡
            return serviceUrlList.stream().map(url -> {
                String[] socketAddressArray = url.split(":");
                String host = socketAddressArray[0];
                int port = Integer.parseInt(socketAddressArray[1]);
                // 使用Builder构建对象
                return ServiceInstance.builder()
                        .serviceName(serviceName)
                        .host(host)
                        .port(port)
                        // .weight(100) // 以后如果ZK里存了权重，可以在这里解析
                        .build();
            }).collect(Collectors.toList());
        }catch (Exception e){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND, servicePath);
        }
    }
}


package com.jiashi.rpc.core.registry;

import com.jiashi.rpc.common.extension.SPI;

import java.net.InetSocketAddress;
import java.util.List;

@SPI("zookeeper")
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    List<ServiceInstance> lookupService(String serviceName);
}

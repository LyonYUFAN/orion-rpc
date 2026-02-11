package com.jiashi.rpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress lookupService(String serviceName);
}

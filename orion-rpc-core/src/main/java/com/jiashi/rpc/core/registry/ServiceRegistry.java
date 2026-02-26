package com.jiashi.rpc.core.registry;

import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

public interface ServiceRegistry {

    /**
     * 将服务注册到注册中心
     * @param serviceName 服务名称 (如 com.jiashi.UserService)
     * @param inetSocketAddress 服务地址 (如 127.0.0.1:8080)
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}

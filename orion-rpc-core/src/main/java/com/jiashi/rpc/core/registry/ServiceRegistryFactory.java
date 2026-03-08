package com.jiashi.rpc.core.registry;

import com.jiashi.rpc.common.extension.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册中心获取工厂
 * 对外屏蔽底层SPI加载逻辑，一键获取服务注册与发现组件
 */
@Slf4j
public class ServiceRegistryFactory {

    /**
     * 根据配置文件的 key 获取具体的注册中心单例对象
     *
     * @param registryName 注册中心名称（如 "zookeeper", "nacos"）
     * @return ServiceRegistry 实例
     */
    public static ServiceRegistry getServiceRegistry(String registryName) {
        if (registryName == null || registryName.trim().isEmpty()) {
            throw new IllegalArgumentException("ServiceRegistry name cannot be null or empty.");
        }
        return ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(registryName);
    }
}
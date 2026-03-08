package com.jiashi.rpc.core.loadbalancer;

import com.jiashi.rpc.common.extension.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 负载均衡器获取工厂
 * 对外屏蔽ExtensionLoader的加载细节，提供傻瓜式API
 */
@Slf4j
public class LoadBalancerFactory {

    /**
     * 根据配置文件的 key 获取具体的负载均衡器单例对象
     *
     * @param loadBalanceName 负载均衡策略名称（如 "random", "roundRobin"）
     * @return 负载均衡器实例（天生自带你的 AbstractLoadBalancer 模板逻辑）
     */
    public static LoadBalancer getLoadBalancer(String loadBalanceName) {
        if (loadBalanceName == null || loadBalanceName.trim().isEmpty()) {
            throw new IllegalArgumentException("LoadBalancer name cannot be null or empty.");
        }
        return ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension(loadBalanceName);
    }
}
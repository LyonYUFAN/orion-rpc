package com.jiashi.rpc.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 * 作用：存储 接口名 -> 实现类对象 的映射
 */
public class LocalRegistry {

    // 存数据的容器 (Key: 接口全限定名, Value: 实现类实例)
    private static final Map<String, Object> services = new ConcurrentHashMap<>();

    public static void register(String interfaceName, Object service){
        services.put(interfaceName, service);
    }

    public static Object getService(String interfaceName){
        return services.get(interfaceName);
    }

}


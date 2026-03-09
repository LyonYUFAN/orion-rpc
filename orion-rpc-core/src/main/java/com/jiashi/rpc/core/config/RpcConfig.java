package com.jiashi.rpc.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Data
@Slf4j // 引入日志，方便提示配置加载状态
@AllArgsConstructor // 必须增加这个，让 Spring 能通过构造器注入
@NoArgsConstructor  // 保留无参，用于纯 Java 环境下的默认初始化
public class RpcConfig {

    // --- 1. 基础网络配置 ---
    private String serverHost = "127.0.0.1";
    private int serverPort = 7777;
    private String zkAddress = "127.0.0.1:2181";

    // --- 2. 支撑 SPI 动态加载的核心配置项 ---
    private String registryType = "zookeeper";
    private String loadBalanceType = "roundRobin";
    private String serializationType = "protostuff";

    // --- 3. 全局单例实例 ---
    private static volatile RpcConfig instance;

    // --- 4. 获取单例 ---
    public static RpcConfig getInstance() {
        if (instance == null) {
            synchronized (RpcConfig.class) {
                if (instance == null) {
                    instance = new RpcConfig();
                    // 事实：将加载逻辑剥离到一个独立的方法中，保持构造代码块清爽
                    loadFromProperties(instance);
                }
            }
        }
        return instance;
    }

    // --- 5. 核心后门 留给Spring Boot Starter用的覆盖方法 ---
    public static void setInstance(RpcConfig customConfig) {
        instance = customConfig;
    }

    // --- 6. 纯Java环境的本地配置加载逻辑 ---
    private static void loadFromProperties(RpcConfig config) {
        Properties properties = new Properties();
        // 尝试从 ClassPath 根目录下读取 rpc.properties
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rpc.properties")) {
            if (is == null) {
                // 如果文件不存在，直接返回，继续使用对象的默认值
                log.info("未发现外部配置文件 rpc.properties，将使用框架默认配置。");
                return;
            }

            properties.load(is);
            log.info("成功加载外部配置文件 rpc.properties。");

            // 逐项读取配置文件，如果配置了确切的值，则覆盖默认值
            String serverHost = properties.getProperty("rpc.server.host");
            if (serverHost != null && !serverHost.trim().isEmpty()) {
                config.setServerHost(serverHost.trim());
            }

            String serverPort = properties.getProperty("rpc.server.port");
            if (serverPort != null && !serverPort.trim().isEmpty()) {
                config.setServerPort(Integer.parseInt(serverPort.trim())); // 注意强转类型
            }

            String zkAddress = properties.getProperty("rpc.zk.address");
            if (zkAddress != null && !zkAddress.trim().isEmpty()) {
                config.setZkAddress(zkAddress.trim());
            }

            String registryType = properties.getProperty("rpc.registry.type");
            if (registryType != null && !registryType.trim().isEmpty()) {
                config.setRegistryType(registryType.trim());
            }

            String loadBalanceType = properties.getProperty("rpc.loadbalance.type");
            if (loadBalanceType != null && !loadBalanceType.trim().isEmpty()) {
                config.setLoadBalanceType(loadBalanceType.trim());
            }

            String serializationType = properties.getProperty("rpc.serialization.type");
            if (serializationType != null && !serializationType.trim().isEmpty()) {
                config.setSerializationType(serializationType.trim());
            }

        } catch (Exception e) {
            // 如果解析格式错误（比如端口填了字母），捕获异常，避免服务无法启动
            log.warn("读取 rpc.properties 发生异常，部分配置将回退到默认值。", e);
        }
    }
}
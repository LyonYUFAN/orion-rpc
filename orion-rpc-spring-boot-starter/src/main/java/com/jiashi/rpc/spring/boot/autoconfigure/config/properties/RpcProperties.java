package com.jiashi.rpc.spring.boot.autoconfigure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "orion-rpc") // 配置文件中的前缀
public class RpcProperties {
    /**
     * 服务端主机名
     */
    private String serverHost = "127.0.0.1";
    /**
     * 服务端端口号
     */
    private int serverPort = 7777;
    /**
     * Zookeeper 地址
     */
    private String zkAddress = "127.0.0.1:2181";

    /**
     * 序列化算法，默认为 protostuff
     */
    private String serializer = "json";

    /**
     * 负载均衡算法，默认为 random
     */
    private String loadBalancer = "random";

    /**
     * 注册中心实现，默认为 zookeeper
     */
    private String registry = "zookeeper";
}
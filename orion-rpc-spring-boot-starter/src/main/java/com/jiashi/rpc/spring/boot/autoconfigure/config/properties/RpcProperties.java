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
}
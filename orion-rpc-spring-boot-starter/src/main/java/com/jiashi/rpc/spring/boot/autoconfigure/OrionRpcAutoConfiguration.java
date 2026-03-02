package com.jiashi.rpc.spring.boot.autoconfigure;

import com.jiashi.rpc.core.config.RpcConfig;
import com.jiashi.rpc.core.transport.server.NettyServer;
import com.jiashi.rpc.spring.boot.autoconfigure.config.properties.RpcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class OrionRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcConfig rpcConfig(RpcProperties properties) {
        // 将 Spring 读取到的配置值传给你的核心 RpcConfig
        return new RpcConfig(
                properties.getServerHost(),
                properties.getServerPort(),
                properties.getZkAddress()
        );
    }

    // 2. 将 NettyServer 转化为 Spring Bean，并自动关联 RpcConfig
    @Bean
    @ConditionalOnMissingBean
    public NettyServer nettyServer(RpcConfig rpcConfig) {
        return new NettyServer(rpcConfig);
    }
}


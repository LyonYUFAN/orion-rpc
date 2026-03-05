package com.jiashi.rpc.spring.boot.autoconfigure;

import com.jiashi.rpc.core.config.RpcConfig;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import com.jiashi.rpc.core.registry.zk.ZkServiceRegistryImpl;
import com.jiashi.rpc.core.transport.client.NettyClient;
import com.jiashi.rpc.core.transport.client.proxy.RpcClientProxy;
import com.jiashi.rpc.core.transport.server.NettyServer;
import com.jiashi.rpc.spring.boot.autoconfigure.bootstrap.RpcServerBootstrap;
import com.jiashi.rpc.spring.boot.autoconfigure.config.properties.RpcProperties;
import com.jiashi.rpc.spring.boot.autoconfigure.processor.RpcSpringPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    // 新增事实 2-A：注入 NettyClient (假设你的 NettyClient 有无参构造)
    @Bean
    @ConditionalOnMissingBean
    public NettyClient nettyClient() {
        return new NettyClient();
    }

    // 注册中心Bean(依赖RpcConfig中的ZK地址)
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        // 实例化ZK注册中心,具体实现取决于你的构造函数
        return new ZkServiceRegistryImpl();
    }

    // 将NettyServer转化为Spring Bean，并自动关联RpcConfig
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "orion-rpc.server-port")
    public NettyServer nettyServer(RpcConfig rpcConfig) {
        return new NettyServer(rpcConfig);
    }

    // 客户端代理Bean(依赖注册中心/服务发现)
    @Bean
    @ConditionalOnMissingBean
    public RpcClientProxy rpcClientProxy(NettyClient nettyClient) {
        return new RpcClientProxy(nettyClient);
    }

    // 以下为Spring生态深度集成组件
    @Bean
    @ConditionalOnProperty(name = "orion-rpc.server-port")
    public RpcServerBootstrap rpcServerBootstrap(NettyServer server) {
        return new RpcServerBootstrap(server);
    }

    // 自定义注解后置处理器
    @Bean
    public RpcSpringPostProcessor rpcSpringPostProcessor() {
        return new RpcSpringPostProcessor();
    }
}


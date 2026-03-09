package com.jiashi.rpc.spring.boot.autoconfigure;

import com.jiashi.rpc.common.extension.ExtensionLoader;
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
        RpcConfig config = new RpcConfig(
                properties.getServerHost(),
                properties.getServerPort(),
                properties.getZkAddress(),
                properties.getRegistry(),      // 传给 registryType
                properties.getLoadBalancer(),  // 传给 loadBalanceType
                properties.getSerializer()     // 传给 serializationType
        );
        // 核心事实:将生成的Bean强行塞给RpcConfig的静态单例
        // 这样,非Spring管理的RpcClientProxy以后通过getInstance()拿到的就是这份YAML配置了
        RpcConfig.setInstance(config);
        return config;
    }

    // 新增事实 2-A：注入 NettyClient (假设你的 NettyClient 有无参构造)
    @Bean
    @ConditionalOnMissingBean
    public NettyClient nettyClient() {
        return new NettyClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry(RpcConfig rpcConfig) {
        // 通过 SPI 动态加载，而不是 new 一个固定的实现
        return ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(rpcConfig.getRegistryType());
    }

    // 将NettyServer转化为Spring Bean，并自动关联RpcConfig
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "orion-rpc.server-port")
    public NettyServer nettyServer(RpcConfig rpcConfig) {
        return new NettyServer(rpcConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    // 【核心事实修改】：我们在参数列表里强制加上 RpcConfig rpcConfig
    // 这等同于拿枪指着 Spring 说：“在创建 RpcClientProxy 之前，必须先把 rpcConfig 给我建好！”
    public RpcClientProxy rpcClientProxy(NettyClient nettyClient, RpcConfig rpcConfig) {

        // 增加一行测试打印，用来确认配置是否真的拿到了
        System.out.println("====== Spring 注入代理时，当前全局序列化配置为: " +
                RpcConfig.getInstance().getSerializationType() + " ======");

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


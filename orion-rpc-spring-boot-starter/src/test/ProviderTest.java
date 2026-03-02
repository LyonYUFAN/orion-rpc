package com.jiashi.rpc.core;

import com.jiashi.rpc.core.transport.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.jiashi.rpc") // 确保扫描到你的 @RpcService 和 RpcSpringPostProcessor
public class ProviderTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProviderTest.class);
        System.out.println("Spring 容器启动成功...");

        NettyServer nettyServer = context.getBean(NettyServer.class);

        nettyServer.start("127.0.0.1", 7777);
    }
}
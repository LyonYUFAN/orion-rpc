package com.jiashi.rpc.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.jiashi.rpc") // 扫包，让代理类、客户端、Zk注册类都被加载
public class ConsumerTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConsumerTest.class);
        System.out.println("客户端 Spring 容器启动成功...");

        ClientTestController controller = context.getBean(ClientTestController.class);

        controller.testRpcCall();
    }
}
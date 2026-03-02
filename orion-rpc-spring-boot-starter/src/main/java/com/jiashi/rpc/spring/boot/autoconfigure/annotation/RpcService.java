package com.jiashi.rpc.spring.boot.autoconfigure.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RPC 服务提供者注解
 * 事实标准：添加 @Component 使其自动被 Spring 扫描并注册为 Bean
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 它只能修饰类、接口（包括注解类型）或枚举。
@Component
public @interface RpcService {
    String version() default "1.0.0";
    String group() default "default";
}

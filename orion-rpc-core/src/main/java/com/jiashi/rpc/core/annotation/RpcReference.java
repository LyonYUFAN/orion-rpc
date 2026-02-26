package com.jiashi.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * RPC 服务消费者注解 (用于注入代理对象)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD}) // 只能用来修饰类的成员变量
public @interface RpcReference {
    String version() default "1.0.0";
    String group() default "default";
}

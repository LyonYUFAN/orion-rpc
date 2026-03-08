package com.jiashi.rpc.common.extension;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.*;

/**
 * 标明这是一个 SPI 扩展接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
    // 默认的扩展点名称，如果不传任何 name 获取扩展点，就使用这个默认值
    String value() default "";
}

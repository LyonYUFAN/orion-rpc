package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.extension.ExtensionLoader;
import com.jiashi.rpc.common.serializer.impl.ProtostuffSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensionSpiTest {

    @Test
    public void testGetExtension() {
        System.out.println("--- 事实验证 1：测试 SPI 基本加载能力 ---");
        Serializer serializer1 = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("protostuff");

        Assertions.assertNotNull(serializer1, "加载失败：序列化器对象为空！");
        Assertions.assertTrue(serializer1 instanceof ProtostuffSerializer, "加载失败：对象类型不是 ProtostuffSerializer！");

        System.out.println("第一次获取成功，对象内存地址: " + serializer1);

        System.out.println("\n--- 事实验证 2：测试单例缓存 (DCL) 是否生效 ---");
        Serializer serializer2 = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("protostuff");
        System.out.println("第二次获取成功，对象内存地址: " + serializer2);

        Assertions.assertSame(serializer1, serializer2, "缓存失败：SPI 没有返回同一个实例！");
        System.out.println("事实证明：两次获取的是同一个实例，缓存机制完美生效！");
    }

    @Test
    public void testGetExtension_NotFound() {
        System.out.println("\n--- 事实验证 3：测试异常处理机制 ---");
        Assertions.assertThrows(RuntimeException.class, () -> {
            ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("unknown_serializer");
        }, "应该抛出异常，但却没有抛出！");
    }
}
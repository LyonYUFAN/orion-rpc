package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.enums.SerializationType;

public class SpiTest {

    public static void main(String[] args) {
        System.out.println("====== 开始测试 SPI 机制 ======");

        try {
            byte targetCode = SerializationType.PROTOSTUFF.getCode();

            Serializer serializer = SerializerFactory.getSerializer(targetCode);

            // 如果能成功走到这里，说明从 Factory 的 static 代码块中利用 ServiceLoader 加载成功了
            System.out.println("SPI 加载成功！");
            System.out.println("获取到的序列化器实例类名: " + serializer.getClass().getName());
            System.out.println("该序列化器的标识码(Code): " + serializer.getCode());

            // 测试一个不存在的 code，验证容错逻辑
            System.out.println("\n--- 测试获取未配置的序列化器 ---");
            SerializerFactory.getSerializer((byte) 99);
        } catch (IllegalArgumentException e) {
            System.out.println("捕获到预期的异常: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("测试出现意外错误: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("====== 测试结束 ======");
    }
}
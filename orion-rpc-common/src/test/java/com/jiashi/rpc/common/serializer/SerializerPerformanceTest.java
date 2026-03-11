package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.serializer.impl.JsonSerializer;
import com.jiashi.rpc.common.serializer.impl.KryoSerializer;
import com.jiashi.rpc.common.serializer.impl.ProtostuffSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class SerializerPerformanceTest {

    private TestObject testObject;
    private final int ITERATIONS = 100000; // 测试 10 万次

    @BeforeEach
    void setUp() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        testObject = new TestObject("OrionRPC-Test", 23,
                Arrays.asList("Java", "RPC", "Kryo", "Network"), map);
    }

    @Test
    void testPerformance() {
        runTest(new JsonSerializer(), "JSON");
        runTest(new ProtostuffSerializer(), "Protostuff");
        runTest(new KryoSerializer(), "Kryo");
    }

    private void runTest(Serializer serializer, String name) {
        // 1. 预热 (JVM 优化)
        for (int i = 0; i < 10000; i++) {
            byte[] bytes = serializer.serialize(testObject);
            serializer.deserialize(bytes, TestObject.class);
        }

        // 2. 统计序列化体积
        byte[] sampleBytes = serializer.serialize(testObject);
        int byteSize = sampleBytes.length;

        // 3. 测试序列化耗时
        long startSerialize = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            serializer.serialize(testObject);
        }
        long endSerialize = System.currentTimeMillis();

        // 4. 测试反序列化耗时
        long startDeserialize = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            serializer.deserialize(sampleBytes, TestObject.class);
        }
        long endDeserialize = System.currentTimeMillis();

        System.out.println(String.format("[%s] 结果:", name));
        System.out.println("序列化后体积: " + byteSize + " bytes");
        System.out.println("10万次序列化耗时: " + (endSerialize - startSerialize) + " ms");
        System.out.println("10万次反序列化耗时: " + (endDeserialize - startDeserialize) + " ms");
        System.out.println("---------------------------------------");
    }
}
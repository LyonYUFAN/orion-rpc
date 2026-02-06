package com.jiashi.rpc.common.serializer.impl;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.jiashi.rpc.common.serializer.Serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 Protostuff 的序列化实现
 */
public class ProtostuffSerializer implements Serializer {

    // 避免每次序列化都重新分配内存，使用 ThreadLocal 复用 Buffer
    private static final ThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL = ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    // 缓存 Schema，提高性能 (核心优化点)
    private static final Map<Class<?>, Schema<?>> SCHEMA_CACHE = new ConcurrentHashMap<>();

    @Override
    public byte[] serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        Schema schema = getSchema(clazz);
        LinkedBuffer buffer = BUFFER_THREAD_LOCAL.get();
        try {
            // 核心逻辑：把 obj 按照 schema 规则转化成字节数组
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear(); // 必须清理
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        // 将字节数组的数据填充到对象中
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    /**
     * 获取 Schema (类似获取类的元数据)
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) SCHEMA_CACHE.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.getSchema(clazz);
            SCHEMA_CACHE.put(clazz, schema);
        }
        return schema;
    }
}
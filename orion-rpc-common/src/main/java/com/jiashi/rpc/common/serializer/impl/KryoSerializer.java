package com.jiashi.rpc.common.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jiashi.rpc.common.serializer.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {

    /**
     * Kryo 线程不安全，使用 ThreadLocal 保证并发安全
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 设置是否关闭循环引用检查，如果关闭可以提高性能，但如果对象中有循环引用会栈溢出
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte getCode() {
        return (byte) 3;
    }

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Kryo 序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Kryo 反序列化失败", e);
        }
    }
}
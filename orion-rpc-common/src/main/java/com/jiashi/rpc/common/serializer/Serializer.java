package com.jiashi.rpc.common.serializer;

public interface Serializer {

    /**
     * 序列化：把对象变成字节数组 (装箱)
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化：把字节数组变回对象 (开箱)
     * @param bytes 字节数组
     * @param clazz 目标对象的类类型
     * @param <T>   泛型
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}

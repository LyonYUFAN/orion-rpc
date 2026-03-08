package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.extension.SPI;

@SPI("protostuff")
public interface Serializer {

    /**
     * 获取当前序列化算法的唯一标识码
     * @return 序列化代码 (如 JSON=1, PROTOSTUFF=2)
     */
    byte getCode();

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

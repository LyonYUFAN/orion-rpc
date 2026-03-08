package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.serializer.impl.ProtostuffSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class SerializerFactory {

    private static final Map<Byte, Serializer> serializerMap = new HashMap<>();

    static {

        // 使用Java原生的SPI加载机制
        // 它会去classpath下的META-INF/services/目录寻找配置文件
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);

        for (Serializer serializer : serviceLoader) {
            serializerMap.put(serializer.getCode(), serializer);
        }
    }

    public static Serializer getSerializer(byte codec) {
        Serializer serializer = serializerMap.get(codec);
        if(serializer == null){
             throw new IllegalArgumentException("Unsupported serializer code: " + codec);
        }
        return serializer;
    }
}


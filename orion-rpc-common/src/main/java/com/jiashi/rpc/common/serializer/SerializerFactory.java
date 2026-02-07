package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.serializer.impl.ProtostuffSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    /**
     * 序列化算法的 ID (必须和 ProtocolConstants 里的定义对应)
     */
    public static final byte JSON = 1;
    public static final byte PROTOSTUFF = 2;
    public static final byte HESSIAN = 3;
    private static final Map<Byte, Serializer> serializerMap = new HashMap<>();

    static {
        // 这里注册你所有的序列化器
        // 1. JSON (暂时还没有实现，先注释掉或者先用 Protostuff 代替)
        // serializerMap.put(JSON, new JsonSerializer());

        // 2. Protostuff (你截图里已经有的)
        serializerMap.put(PROTOSTUFF, new ProtostuffSerializer());

        // 3. Hessian (待实现)
        // serializerMap.put(HESSIAN, new HessianSerializer());
    }

    public static Serializer getSerializer(byte codec) {
        Serializer serializer = serializerMap.get(codec);
        if(serializer == null){
             throw new IllegalArgumentException("Unsupported serializer code: " + codec);
        }
        return serializer;
    }
}


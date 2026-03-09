package com.jiashi.rpc.common.enums;

import lombok.Getter;

/**
 * 序列化类型枚举
 */
@Getter
public enum SerializationType {

    JSON((byte) 1, "json"),
    PROTOSTUFF((byte) 2, "protostuff"),
    HESSIAN((byte) 3, "hessian");

    private final byte code;
    private final String name;

    // 手写构造器：确保 Maven 编译时能正确识别参数
    SerializationType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    // 手写 Getter：确保 RpcMessageEncoder 调用 getCode() 不报错
    public byte getCode() {
        return code;
    }

    public static SerializationType getEnum(byte code) {
        for (SerializationType c : SerializationType.values()) {
            if (c.getCode() == code) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown serialization code: " + code);
    }
}
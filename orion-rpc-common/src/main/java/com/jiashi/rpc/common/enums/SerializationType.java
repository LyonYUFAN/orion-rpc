package com.jiashi.rpc.common.enums;

/**
 * 序列化类型枚举
 */
public enum SerializationType {

    JSON((byte) 1),
    PROTOSTUFF((byte) 2),
    HESSIAN((byte) 3);

    private final byte code;

    // 手写构造器：确保 Maven 编译时能正确识别参数
    SerializationType(byte code) {
        this.code = code;
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
        return null;
    }
}
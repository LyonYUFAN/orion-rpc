package com.jiashi.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationType {

    JSON((byte) 1),
    PROTOSTUFF((byte) 2),
    HESSIAN((byte) 3);

    private final byte code;

    public static SerializationType getEnum(byte code) {
        for (SerializationType c : SerializationType.values()) {
            if (c.getCode() == code) {
                return c;
            }
        }
        return null;
    }
}

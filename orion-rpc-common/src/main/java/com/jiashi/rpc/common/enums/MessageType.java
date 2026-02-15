package com.jiashi.rpc.common.enums;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;

public enum MessageType {

    REQUEST((byte) 1, RpcRequest.class),
    RESPONSE((byte) 2, RpcResponse.class),
    HEARTBEAT((byte) 3, null);

    private final byte code;
    private final Class<?> contentClass;

    // 手写构造器，确保 Maven 一定能识别
    MessageType(byte code, Class<?> contentClass) {
        this.code = code;
        this.contentClass = contentClass;
    }

    // 手写 Getter
    public byte getCode() {
        return code;
    }

    public Class<?> getContentClass() {
        return contentClass;
    }

    public static MessageType findByCode(byte code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
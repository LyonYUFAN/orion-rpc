package com.jiashi.rpc.common.enums;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {

    /**
     * 请求消息 -> 对应 RpcRequest 类
     */
    REQUEST((byte) 1, RpcRequest.class),

    /**
     * 响应消息 -> 对应 RpcResponse 类
     */
    RESPONSE((byte) 2, RpcResponse.class),

    /**
     * 心跳消息 -> 不需要反序列化 Body (或者对应 null)
     */
    HEARTBEAT((byte) 3, null);

    private final byte code;
    private final Class<?> contentClass;

    public static MessageType findByCode(byte code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null; // 或者抛出异常
    }
}

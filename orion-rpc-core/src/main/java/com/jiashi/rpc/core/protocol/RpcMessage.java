package com.jiashi.rpc.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//RpcMessage是信封,用来在TCP网卡中传输信息
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcMessage {

    /**
     * 消息类型 (1:请求 / 2:响应 / 3:心跳)
     */
    private byte messageType;

    /**
     * 序列化类型 (1:JSON / 2:Protostuff / 3:Hessian)
     */
    private byte codec;

    /**
     * 压缩类型 (0:无 / 1:Gzip) - 预留字段，目前可以先不处理
     */
    private byte compress;

    /**
     * 请求ID (用于将响应匹配回请求)
     */
    private int requestId;

    /**
     * 数据内容 (被序列化后的 RpcRequest 或 RpcResponse 字节数组)
     */
    private Object data;

}


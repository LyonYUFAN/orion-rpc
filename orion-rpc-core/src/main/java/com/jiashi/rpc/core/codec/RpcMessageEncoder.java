package com.jiashi.rpc.core.codec;

import com.jiashi.rpc.common.serializer.Serializer;
import com.jiashi.rpc.common.serializer.SerializerFactory;
import com.jiashi.rpc.core.protocol.ProtocolConstants;
import com.jiashi.rpc.core.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器：RpcMessage -> ByteBuf
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        // 1. 写入头部信息
        out.writeBytes(ProtocolConstants.MAGIC_NUMBER);
        out.writeByte(ProtocolConstants.VERSION);
        out.writeByte(rpcMessage.getMessageType());
        out.writeByte(rpcMessage.getCodec());
        out.writeByte(rpcMessage.getCompress());
        out.writeInt(rpcMessage.getRequestId());

        // 2. 序列化 Body (如果是心跳包，Body 为空，不做序列化)
        byte[] bodyBytes = null;

        if (rpcMessage.getData() != null) {
            Serializer serializer = SerializerFactory.getSerializer(rpcMessage.getCodec());
            bodyBytes = serializer.serialize(rpcMessage.getData());
        }

        // 3. 写入 Body 长度和数据
        int length = (bodyBytes == null) ? 0 : bodyBytes.length;
        out.writeInt(length);

        if (bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }
    }
}
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
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        //这里的顺序必须和 ProtocolConstants 定义的一致
        byteBuf.writeBytes(ProtocolConstants.MAGIC_NUMBER);
        byteBuf.writeByte(ProtocolConstants.VERSION);
        byteBuf.writeByte(rpcMessage.getMessageType());
        byteBuf.writeByte(rpcMessage.getCodec());
        byteBuf.writeByte(rpcMessage.getCompress());
        byteBuf.writeInt(rpcMessage.getRequestId());

        Serializer serializer = SerializerFactory.getSerializer(rpcMessage.getCodec());
        byte[] bodyBytes = serializer.serialize(rpcMessage.getData());
        byteBuf.writeInt(bodyBytes.length);
        byteBuf.writeBytes(bodyBytes);
    }
}


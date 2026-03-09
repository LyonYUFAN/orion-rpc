package com.jiashi.rpc.core.codec;

import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.common.extension.ExtensionLoader;
import com.jiashi.rpc.common.serializer.Serializer;
import com.jiashi.rpc.common.serializer.SerializerFactory;
import com.jiashi.rpc.core.protocol.ProtocolConstants;
import com.jiashi.rpc.core.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Arrays;


/**
 * 解码器：ByteBuf -> RpcMessage
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // 1. maxFrameLength: 8MB (包太大了我不收)
        // 2. lengthFieldOffset: 12 (跳过前面12个字节，才是长度字段)
        // 3. lengthFieldLength: 4 (长度字段本身占4个字节)
        // 4. lengthAdjustment: 0 (不需要调整)
        // 5. initialBytesToStrip: 0 (不要把头部切掉，我全都要)
        super(8 * 1024 * 1024, 12, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 核心封装代码
        // 切割好的decoded正好就是我们的RpcMessage对象的字节数
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            try {
                return decodeFrame(frame);
            } finally {
                frame.release(); // 记得释放内存！
            }
        }
        return decoded;
    }

    private RpcMessage decodeFrame(ByteBuf in) {

        checkMagicNumber(in);
        checkVersion(in);

        byte messageType = in.readByte();
        byte codec = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        int dataLength = in.readInt();

        // 读取 Body
        Object body = null;
        if (dataLength > 0) {
            byte[] bodyBytes = new byte[dataLength];
            in.readBytes(bodyBytes);

            SerializationType type = SerializationType.getEnum(codec);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(type.getName());
            MessageType msgTypeEnum = MessageType.findByCode(messageType);
            if (msgTypeEnum == null) {
                throw new IllegalArgumentException("Unknown message type: " + messageType);
            }

            Class<?> targetClass = msgTypeEnum.getContentClass();
            if (targetClass != null) {
                body = serializer.deserialize(bodyBytes, targetClass);
            }
        }

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(messageType);
        rpcMessage.setCodec(codec);
        rpcMessage.setCompress(compressType);
        rpcMessage.setRequestId(requestId);
        rpcMessage.setData(body);
        return rpcMessage;
    }

    private void checkMagicNumber(ByteBuf in) {
        // 读取前4个字节
        byte[] tmp = new byte[ProtocolConstants.MAGIC_NUMBER.length];
        in.readBytes(tmp);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != ProtocolConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != ProtocolConstants.VERSION) {
            throw new IllegalArgumentException("Protocol version not support: " + version);
        }
    }
}


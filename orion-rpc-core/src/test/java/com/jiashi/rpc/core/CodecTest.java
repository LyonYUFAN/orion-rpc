package com.jiashi.rpc.core;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.serializer.SerializerFactory;
import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodecTest {

    public static void main(String[] args) {
        // 1. 创建一个嵌入式通道 (专门用来测试 Handler)
        // 只要把咱们写好的 编码器 和 解码器 塞进去即可
        EmbeddedChannel channel = new EmbeddedChannel(
                new RpcMessageDecoder(), // 入站解码
                new RpcMessageEncoder()  // 出站编码
        );

        // 2. 准备一个假的请求消息 (模拟业务层产生的数据)
        RpcRequest request = new RpcRequest();
        request.setInterfaceName("com.jiashi.UserService");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"World"});
        request.setParamTypes(new Class[]{String.class});
        request.setRequestId(12345); // 确保你的 RpcRequest 里 requestId 是 Integer

        RpcMessage protocolMsg = new RpcMessage();
        protocolMsg.setCodec(SerializerFactory.PROTOSTUFF);
        protocolMsg.setCompress((byte) 0);
        protocolMsg.setMessageType(MessageType.REQUEST.getCode());
        protocolMsg.setRequestId(12345);
        protocolMsg.setData(request);

        // =================================================================
        // 测试环节 A: 测试编码 (出站: Object -> ByteBuf)
        // =================================================================
        log.info(">>> 开始测试编码...");
        // writeOutbound 模拟向网络发送数据
        channel.writeOutbound(protocolMsg);

        // 读取通道里产生的字节数据
        ByteBuf outputBuf = channel.readOutbound();

        log.info("编码成功！产生的数据包长度: {} 字节", outputBuf.readableBytes());
        // 简单校验一下魔数 (前4个字节)
        byte[] magic = new byte[4];
        outputBuf.getBytes(0, magic);
        log.info("魔数校验: {}", bytesToHex(magic)); // 应该是 6F 72 69 63


        // =================================================================
        // 测试环节 B: 测试解码 (入站: ByteBuf -> Object)
        // =================================================================
        log.info(">>> 开始测试解码...");
        // writeInbound 模拟从网络收到数据 (我们直接把刚才编码好的字节写回去)
        // 注意：因为 ByteBuf 是引用计数的，刚才 readOutbound 出来引用计数没变，我们增加一次引用或者直接用
        channel.writeInbound(outputBuf);

        // 读取解码后的对象
        RpcMessage decodedMsg = channel.readInbound();

        log.info("解码成功！收到对象: {}", decodedMsg);

        // =================================================================
        // 验证结果
        // =================================================================
        if (decodedMsg.getRequestId() == 12345 && decodedMsg.getData() instanceof RpcRequest) {
            log.info("✅ 测试通过！请求ID匹配，Body类型正确。");
            RpcRequest req = (RpcRequest) decodedMsg.getData();
            log.info("还原出的方法名: {}", req.getMethodName());
        } else {
            log.error("❌ 测试失败！数据不匹配。");
        }
    }

    // 辅助方法：打印十六进制
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
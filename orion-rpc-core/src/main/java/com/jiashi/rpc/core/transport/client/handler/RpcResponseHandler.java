package com.jiashi.rpc.core.transport.client.handler;

import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.core.protocol.ProtocolConstants;
import com.jiashi.rpc.core.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        // 1. 如果是心跳响应 (Pong)，直接忽略
        if (msg.getMessageType() == MessageType.HEARTBEAT.getCode()) {
            log.debug("收到服务端心跳响应 Pong");
            return;
        }
        log.info("收到服务端响应: {}", msg);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("客户端触发写空闲，发送心跳包 Ping...");

                RpcMessage heartbeat = new RpcMessage();
                heartbeat.setMessageType(MessageType.HEARTBEAT.getCode());
                heartbeat.setCodec((byte) 1);
                heartbeat.setCompress((byte) 0);
                heartbeat.setRequestId(ProtocolConstants.HEARTBEAT_ID);
                heartbeat.setData(null);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
package com.jiashi.rpc.core.transport.client.handler;

import com.jiashi.rpc.common.entity.RpcResponse;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.core.protocol.ProtocolConstants;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.transport.client.UnprocessedRequests;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        if (msg.getMessageType() == MessageType.HEARTBEAT.getCode()) {
            log.debug("收到服务端心跳响应 Pong");
            return;
        }

        //log.info("收到服务端响应: {}", msg);

        if (msg.getMessageType() == MessageType.RESPONSE.getCode()) {
            RpcResponse response = (RpcResponse) msg.getData();
            // 直接调用修正后的静态方法
            UnprocessedRequests.complete(response);
        }
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
                heartbeat.setRequestId(ProtocolConstants.HEARTBEAT_ID); // 注意类型匹配
                heartbeat.setData(null);

                // 【修复】：组装好心跳包后，必须把它写进通道发出去！加上监听器以便在发送失败时关闭连接
                ctx.writeAndFlush(heartbeat).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("发送心跳包失败", future.cause());
                        ctx.close();
                    }
                });
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("RPC 客户端发生异常", cause);
        ctx.close();
    }
}
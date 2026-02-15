package com.jiashi.rpc.core.transport.client.initializer;

import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.transport.client.handler.RpcResponseHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcResponseInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 【新增】空闲检测
        // 参数1 (readerIdleTime): 0 表示不关心
        // 参数2 (writerIdleTime): 写空闲时间。如果 30秒 没有写数据，会触发 WRITER_IDLE 事件
        // 参数3 (allIdleTime): 0 表示不关心
        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        pipeline.addLast(new RpcMessageDecoder());
        pipeline.addLast(new RpcMessageEncoder());
        pipeline.addLast(new RpcResponseHandler());
    }
}


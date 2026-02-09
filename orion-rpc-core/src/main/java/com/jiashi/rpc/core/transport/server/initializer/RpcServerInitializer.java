package com.jiashi.rpc.core.transport.server.initializer;

import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.transport.server.handler.RpcRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务端流水线初始化配置
 * 作用：将一个个 Handler 组装到 Pipeline 中
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();;
        pipeline.addLast(new RpcMessageDecoder());
        pipeline.addLast(new RpcMessageEncoder());
        // 业务处理器
        pipeline.addLast(new RpcRequestHandler());
    }
}


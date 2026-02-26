package com.jiashi.rpc.core.transport.server.initializer;

import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.transport.server.handler.RpcRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 服务端流水线初始化配置
 * 作用：将一个个 Handler 组装到 Pipeline 中
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();;
        //pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));
        // 【新增】空闲检测
        // 参数1 (readerIdleTime): 读空闲时间。如果 90秒 没有读到数据，会触发 READER_IDLE 事件
        // 参数2 (writerIdleTime): 写空闲时间。0 表示不关心
        // 参数3 (allIdleTime): 读或写空闲时间。0 表示不关心
        pipeline.addLast(new IdleStateHandler(90, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new RpcMessageDecoder());
        pipeline.addLast(new RpcMessageEncoder());
        // 业务处理器
        pipeline.addLast(new RpcRequestHandler());
    }
}


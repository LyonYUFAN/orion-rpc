package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.provider.impl.HelloServiceImpl;
import com.jiashi.rpc.core.transport.client.handler.RpcResponseHandler;
import com.jiashi.rpc.core.transport.client.initializer.RpcResponseInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient {

    private final String host;
    private final int port;
    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private Channel channel;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new RpcResponseInitializer());
    }

    public void connect() {
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();
            log.info("连接服务端成功 {}:{}", host, port);
        } catch (InterruptedException e) {
            log.error("连接服务端失败", e);
        }
    }

    public void sendRequest(RpcMessage rpcMessage) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("发送消息成功, ID: {}", rpcMessage.getRequestId());
                } else {
                    log.error("发送消息失败: ", future.cause());
                }
            });
        } else {
            throw new IllegalStateException("通道未建立或已关闭");
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        // 3. 关闭时，关闭的是属于这个实例的 Group
        group.shutdownGracefully();
    }

}
package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.provider.impl.HelloServiceImpl;
import com.jiashi.rpc.core.registry.ServiceDiscovery;
import com.jiashi.rpc.core.registry.ServiceInstance;
import com.jiashi.rpc.core.registry.zk.ZkServiceDiscoveryImpl;
import com.jiashi.rpc.core.transport.client.handler.RpcResponseHandler;
import com.jiashi.rpc.core.transport.client.initializer.RpcResponseInitializer;
import com.jiashi.rpc.core.transport.client.proxy.RpcClientProxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyClient {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private final Map<String, Channel> channelCache = new ConcurrentHashMap<>();

    public NettyClient() {
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new RpcResponseInitializer());

    }

    /**
     * 发送 RPC 请求
     */
    @SneakyThrows
    public CompletableFuture<RpcResponse> sendRequest(RpcMessage rpcMessage,InetSocketAddress inetSocketAddress) {
        RpcRequest request = (RpcRequest)rpcMessage.getData();
        // 获取通道 (优先从缓存拿，没有则连接)
        Channel channel = getChannel(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
            UnprocessedRequests.put(request.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("Client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    log.error("Send failed:", future.cause());
                }
            });
            return resultFuture;
        } else {
            throw new IllegalStateException("Failed to get channel for address: " + inetSocketAddress);
        }
    }

    /**
     * 连接服务端的逻辑 (带缓存)
     */
    @SneakyThrows
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();

        // 如果缓存里有且是活跃的，直接用
        if (channelCache.containsKey(key)) {
            Channel channel = channelCache.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            channelCache.remove(key); // 不活跃了就移除
        }

        // 建立新连接
        Channel channel = connect(inetSocketAddress);
        channelCache.put(key, channel);
        return channel;
    }

    @SneakyThrows
    private Channel connect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Connect to server [{}] success!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });

        // 阻塞等待连接成功
        return completableFuture.get();
    }

    public void close() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) {

        NettyClient nettyClient = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
        HelloService service = rpcClientProxy.getProxy(HelloService.class);
        String result = service.hello("jiashi");
        System.out.println("RPC 调用成功！服务端返回结果: " + result);
    }
}
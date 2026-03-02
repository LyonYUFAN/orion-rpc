package com.jiashi.rpc.core.transport;

import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.config.RpcConfig;
import com.jiashi.rpc.core.transport.client.handler.RpcResponseHandler;
import com.jiashi.rpc.core.transport.server.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartbeatTest {

    public static void main(String[] args) throws InterruptedException {
        int PORT = 8095;
        String HOST = "127.0.0.1";

        // 启动服务端 (使用你现有的 Server)
        new Thread(() -> {
            // 服务端的 90秒 超时配置没关系，只要客户端发得比 90秒 快就行
            new NettyServer(new RpcConfig(HOST,PORT)).start();
        }).start();

        // 稍微等一下让服务端起好
        Thread.sleep(2000);

        // 启动一个“特供版”客户端 (自定义5秒心跳)
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            // 下面这些还是复用你写好的
                            ch.pipeline().addLast(new RpcMessageDecoder());
                            ch.pipeline().addLast(new RpcMessageEncoder());
                            ch.pipeline().addLast(new RpcResponseHandler());
                        }
                    });

            log.info("=== 客户端发起连接 ===");
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            log.info("=== 连接成功！请观察控制台，每 5秒 会有一次 Ping-Pong 日志 ===");

            // 阻塞住主线程，不让程序退出，静静等待心跳发生
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
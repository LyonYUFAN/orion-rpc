package com.jiashi.rpc.core.transport.server;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.core.provider.impl.HelloServiceImpl;
import com.jiashi.rpc.core.registry.LocalRegistry;
import com.jiashi.rpc.core.transport.server.initializer.RpcServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {

    public void start(String host,int port){
        start0(host,port);
    }

    public void start0(String host,int port){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new RpcServerInitializer());

            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            log.info("OrionRpc Server start");

            // 阻塞主线程，直到服务器关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("服务端启动报错", e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    //
    public static void main(String[] args) {

        // 注册一个服务
        HelloService helloService = new HelloServiceImpl();
        LocalRegistry.register(HelloService.class.getName(), helloService);
        log.info("✅ 已注册服务: {}", HelloService.class.getName());

        // 启动服务端，监听 8088 端口
        new NettyServer().start("127.0.0.1", 8888);
    }
}


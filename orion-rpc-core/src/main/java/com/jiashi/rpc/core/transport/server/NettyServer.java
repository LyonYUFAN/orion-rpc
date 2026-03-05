package com.jiashi.rpc.core.transport.server;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.core.config.RpcConfig;
import com.jiashi.rpc.core.provider.impl.HelloServiceImpl;
import com.jiashi.rpc.core.provider.LocalRegistry;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import com.jiashi.rpc.core.registry.zk.ZkServiceRegistryImpl;
import com.jiashi.rpc.core.transport.server.initializer.RpcServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyServer {

    private final RpcConfig rpcConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(RpcConfig rpcConfig) {
        this.rpcConfig = rpcConfig;
    }

    public void start(){
        String host = rpcConfig.getServerHost();
        int port = rpcConfig.getServerPort();
        start0(host,port);
    }

    public void start0(String host,int port){
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
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
            if (workerGroup != null) workerGroup.shutdownGracefully();
            if (bossGroup != null) bossGroup.shutdownGracefully();
        }
    }

    public void stop() {
        log.info("OrionRpc 准备执行 Netty 优雅停机...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("OrionRpc Netty 线程池已释放完毕");
    }

}


package com.jiashi.rpc.spring.boot.autoconfigure.bootstrap;

import com.jiashi.rpc.core.transport.server.NettyServer;
import com.jiashi.rpc.spring.boot.autoconfigure.config.properties.RpcProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * 负责在Spring启动完成后拉起 Netty,在Spring关闭时优雅停机Netty
 */
@Slf4j
public class RpcServerBootstrap {

    private final NettyServer nettyServer;
    public RpcServerBootstrap(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    // 专门监听“开业”事件
    @EventListener
    public void onSpringStarted(ContextRefreshedEvent event) {
        log.info("Spring 容器启动完毕，准备异步启动 OrionRPC Netty 服务端...");
        Thread nettyThread = new Thread(() -> {
            try {
                nettyServer.start();
            } catch (Exception e) {
                log.error("Netty 服务端启动异常", e);
            }
        }, "orion-rpc-server-thread");
        nettyThread.setDaemon(true);
        nettyThread.start();
    }

    // 专门监听“倒闭”事件
    @EventListener
    public void onSpringClosed(ContextClosedEvent event) {
        log.info("监听到 Spring 容器关闭事件，执行 OrionRPC 优雅停机...");
        if (nettyServer != null) {
            nettyServer.stop();
        }
    }
}


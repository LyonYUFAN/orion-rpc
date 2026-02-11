package com.jiashi.rpc.core.registry.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * ZooKeeper 客户端工具类
 * 作用：管理 CuratorFramework 的生命周期（创建、连接、关闭），保证全局唯一
 */
public class CuratorUtils {

    private static CuratorFramework zkClient;

    // TODO 这里为了 MVP 直接硬编码，实际生产中应读取配置文件
    private static final String CONNECT_STRING = "127.0.0.1:2181";

    public static CuratorFramework getCuratorClient() {
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // 2. 定义重试策略
        // 参数1: baseSleepTimeMs (基础睡眠时间) = 1000ms
        // 参数2: maxRetries (最大重试次数) = 3次
        // 逻辑: 第1次失败睡1秒，第2次睡2秒，第3次睡4秒... (指数增长，避免雪崩)
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(15000)
                .retryPolicy(retryPolicy)
                .namespace("orion-rpc")
                .build();

        zkClient.start();
        try {
            // 阻塞当前线程，直到连接成功 (或超时)
            // 这一步是为了保证返回的 client 一定是可用的，防止刚启动就报错
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return zkClient;
    }
}


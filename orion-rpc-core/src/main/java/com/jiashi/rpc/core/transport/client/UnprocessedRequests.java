package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义类：未处理请求的“候诊室”
 * 作用：解决 Netty 异步发送、主线程同步等待的问题
 */
public class UnprocessedRequests {

    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public static void put(String requestId, CompletableFuture<RpcResponse> future) {

        CompletableFuture<RpcResponse> oldFuture = UNPROCESSED_RESPONSE_FUTURES.putIfAbsent(requestId, future);

        if (oldFuture != null) {
            // 说明 Map 里已经有个 ID=requestId 的在排队了！
            throw new IllegalStateException("RequestId " + requestId + " 冲突！请检查 ID 生成策略或设置超时机制。");
        }
    }

    public void complete(RpcResponse rpcResponse) {

        CompletableFuture<RpcResponse> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());

        if (future != null) {
            // 这一行执行的瞬间，RpcClientProxy 里卡住的 future.get() 会立马解除阻塞
            future.complete(rpcResponse);
        } else {
            System.err.println("收到了一条找不到归属的响应，ID: " + rpcResponse.getRequestId());
        }
    }
}


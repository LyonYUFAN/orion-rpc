package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.entity.RpcResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static final ConcurrentHashMap<Integer, CompletableFuture<RpcResponse>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public static void put(Integer requestId, CompletableFuture<RpcResponse> future) {
        CompletableFuture<RpcResponse> oldFuture = UNPROCESSED_RESPONSE_FUTURES.putIfAbsent(requestId, future);
        if (oldFuture != null) {
            throw new IllegalStateException("RequestId " + requestId + " 冲突！");
        }
    }

    // 【修正事实 1】：改为 static 方法，方便 Handler 直接调用
    public static void complete(RpcResponse rpcResponse) {
        // 【修正事实 2】：强制转换类型。如果 getRequestId 返回 String，需转为 Integer
        // 这里的转换逻辑要和你 put 进去时的类型严格一致
        Integer requestId = Integer.valueOf(String.valueOf(rpcResponse.getRequestId()));

        CompletableFuture<RpcResponse> future = UNPROCESSED_RESPONSE_FUTURES.remove(requestId);

        if (future != null) {
            future.complete(rpcResponse);
        } else {
            System.err.println("收到了一条找不到归属的响应，ID: " + requestId);
        }
    }
}
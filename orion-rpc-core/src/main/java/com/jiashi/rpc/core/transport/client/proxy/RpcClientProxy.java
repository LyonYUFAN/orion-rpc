package com.jiashi.rpc.core.transport.client.proxy;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.transport.client.NettyClient;
import com.jiashi.rpc.core.transport.client.UnprocessedRequests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcClientProxy implements InvocationHandler {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private final NettyClient nettyClient;

    public RpcClientProxy(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    // 获得代理对象
    public <T> T getProxy(Class<T> clazz){
        T newed = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                this::invoke
        );
        return newed;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest request = new RpcRequest();
        request.setRequestId(ID_GENERATOR.getAndIncrement());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParamTypes(method.getParameterTypes());

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setCodec(SerializationType.PROTOSTUFF.getCode()); // 记得设置序列化方式
        rpcMessage.setCompress((byte) 0);
        rpcMessage.setMessageType(MessageType.REQUEST.getCode()); // 类型是 REQUEST
        rpcMessage.setRequestId(request.getRequestId());
        rpcMessage.setData(request);

        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        UnprocessedRequests.put(String.valueOf(request.getRequestId()), future);
        nettyClient.sendRequest(rpcMessage);
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("RPC 调用等待被中断或发生错误");
            e.printStackTrace();
        }
        if (rpcResponse != null) {
            return rpcResponse.getData();
        }
        return null;
    }
}


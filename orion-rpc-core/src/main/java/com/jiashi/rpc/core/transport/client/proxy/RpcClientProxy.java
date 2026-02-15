package com.jiashi.rpc.core.transport.client.proxy;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.core.loadbalancer.LoadBalancer;
import com.jiashi.rpc.core.loadbalancer.RoundRobinLoadBalancer;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.registry.ServiceDiscovery;
import com.jiashi.rpc.core.registry.ServiceInstance;
import com.jiashi.rpc.core.registry.zk.ZkServiceDiscoveryImpl;
import com.jiashi.rpc.core.transport.client.NettyClient;
import com.jiashi.rpc.core.transport.client.UnprocessedRequests;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final NettyClient nettyClient;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private final ServiceDiscovery serviceDiscovery; // 增加服务发现
    private final LoadBalancer loadBalancer;         // 增加负载均衡

    public RpcClientProxy(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
        // TODO ConsistentHashLoadBalancer实现类
        this.loadBalancer = new RoundRobinLoadBalancer();
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

        List<ServiceInstance> instances = serviceDiscovery.lookupService(request.getInterfaceName());
        ServiceInstance selectedInstance = loadBalancer.select(instances, request);
        if (selectedInstance == null) {
            throw new RuntimeException("No available service provider for: " + request.getInterfaceName());
        }

        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setCodec(SerializationType.PROTOSTUFF.getCode()); // 记得设置序列化方式
        rpcMessage.setCompress((byte) 0);
        rpcMessage.setMessageType(MessageType.REQUEST.getCode()); // 类型是 REQUEST
        rpcMessage.setRequestId(request.getRequestId());
        rpcMessage.setData(request);

        InetSocketAddress targetAddress = new InetSocketAddress(selectedInstance.getHost(), selectedInstance.getPort());

        // 核心修改：重试机制 (Loop Retry)
        int maxRetries = 3;
        int retryCount = 0;
        Throwable lastException = null;

        while (retryCount < maxRetries) {
            try {
                // 发送请求
                CompletableFuture<RpcResponse> future = nettyClient.sendRequest(rpcMessage, targetAddress);

                // 阻塞等待结果 (带超时控制，例如 3秒)
                // 如果 3秒 没结果，抛出 TimeoutException，触发 catch 进入重试
                RpcResponse response = future.get(3, TimeUnit.SECONDS);

                // 检查响应状态
                if (response == null) {
                    throw new RuntimeException("服务响应为空");
                }
                if (response.getCode() != 200) {
                    throw new RuntimeException(response.getMsg());
                }

                // 成功直接返回
                return response.getData();

            } catch (Exception e) {
                retryCount++;
                lastException = e;
                log.warn("RPC 调用失败，正在进行第 {} 次重试... 异常: {}", retryCount, e.getMessage());
            }
        }
        // 重试耗尽，抛出最终异常
        log.error("RPC 调用失败，重试次数已耗尽: {}", method.getName());
        throw new RuntimeException("RPC 调用失败，重试 " + maxRetries + " 次后无果", lastException);
    }
}


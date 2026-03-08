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
        int maxRetries = 3;
        int retryCount = 0;
        Throwable lastException = null;

        while (retryCount < maxRetries) {
            try {
                // 核心修改：将构建请求的逻辑移到循环内部
                // 确保每次重试都去 ID_GENERATOR 获取一个全新的 int ID
                RpcRequest request = new RpcRequest();
                request.setRequestId(ID_GENERATOR.getAndIncrement()); // 比如第一次是 0，重试就是 1，再重试是 2
                request.setInterfaceName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameters(args);
                request.setParamTypes(method.getParameterTypes());

                // 你的负载均衡逻辑可以在循环外，也可以在循环内（如果在循环内，可以实现重试换节点的容错策略）
                List<ServiceInstance> instances = serviceDiscovery.lookupService(request.getInterfaceName());
                ServiceInstance selectedInstance = loadBalancer.select(instances, request);
                if (selectedInstance == null) {
                    throw new RuntimeException("No available services provider for: " + request.getInterfaceName());
                }

                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationType.PROTOSTUFF.getCode());
                rpcMessage.setCompress((byte) 0);
                rpcMessage.setMessageType(MessageType.REQUEST.getCode());
                rpcMessage.setRequestId(request.getRequestId());
                rpcMessage.setData(request);

                InetSocketAddress targetAddress = new InetSocketAddress(selectedInstance.getHost(), selectedInstance.getPort());

                // 发送请求
                CompletableFuture<RpcResponse> future = nettyClient.sendRequest(rpcMessage, targetAddress);

                // 阻塞等待结果
                RpcResponse response = future.get(3, TimeUnit.SECONDS);

                if (response == null) {
                    throw new RuntimeException("服务响应为空");
                }
                if (response.getCode() != 200) {
                    throw new RuntimeException(response.getMsg());
                }

                return response.getData();

            } catch (Exception e) {
                retryCount++;
                lastException = e;
                log.warn("RPC 调用失败，正在进行第 {} 次重试... 异常: {}", retryCount, e.getMessage());
                // 如果你把重试逻辑写得很严谨，这里其实应该调用 UnprocessedRequests 的 remove 方法
                // 把本次失败的 requestId 清理掉，防止内存泄漏。
            }
        }
        log.error("RPC 调用失败，重试次数已耗尽: {}", method.getName());
        throw new RuntimeException("RPC 调用失败，重试 " + maxRetries + " 次后无果", lastException);
    }
}


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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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

        CompletableFuture<RpcResponse> future = nettyClient.sendRequest(rpcMessage,targetAddress);
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


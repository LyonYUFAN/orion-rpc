package com.jiashi.rpc.core.loadbalancer;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.core.registry.ServiceInstance;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    // 也就是个计数器，原子操作保证线程安全
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    protected ServiceInstance doSelect(List<ServiceInstance> instances, RpcRequest request) {
        // 为了防止溢出变成负数，使用位运算 & Integer.MAX_VALUE 也是一种常见写法，或者简单的 Math.abs
        int currentIndex = index.getAndIncrement();
        if (currentIndex < 0) {
            currentIndex = 0;
            index.set(0);
        }
        return instances.get(currentIndex % instances.size());
    }
}
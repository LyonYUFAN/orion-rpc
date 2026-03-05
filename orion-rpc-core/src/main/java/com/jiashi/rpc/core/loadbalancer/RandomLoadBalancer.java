package com.jiashi.rpc.core.loadbalancer;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.core.registry.ServiceInstance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {

    private final Random random = new Random();

    @Override
    protected ServiceInstance doSelect(List<ServiceInstance> instances, RpcRequest request) {
        return instances.get(random.nextInt(instances.size()));
    }
}
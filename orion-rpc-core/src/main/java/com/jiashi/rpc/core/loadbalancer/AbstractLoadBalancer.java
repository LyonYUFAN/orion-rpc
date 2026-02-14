package com.jiashi.rpc.core.loadbalancer;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.core.registry.ServiceInstance;
import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public ServiceInstance select(List<ServiceInstance> instances, RpcRequest request) {
        // 1. 如果列表为空，没得选
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        // 2. 如果只有一台机器，那还选什么？直接给它！
        if (instances.size() == 1) {
            return instances.get(0);
        }
        // 3. 只有多台机器时，才调用具体算法
        return doSelect(instances, request);
    }

    /**
     * 留给子类去实现的具体算法
     */
    protected abstract ServiceInstance doSelect(List<ServiceInstance> instances, RpcRequest request);
}
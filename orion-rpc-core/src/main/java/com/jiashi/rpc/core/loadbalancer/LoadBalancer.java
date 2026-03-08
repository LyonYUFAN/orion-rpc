package com.jiashi.rpc.core.loadbalancer;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.extension.SPI;
import com.jiashi.rpc.core.registry.ServiceInstance;
import java.util.List;

@SPI()
public interface LoadBalancer {
    /**
     * 从服务列表中选择一个实例
     * @param instances 服务列表
     * @param request   请求详情（用于一致性哈希等）
     * @return 选中的实例
     */
    ServiceInstance select(List<ServiceInstance> instances, RpcRequest request);
}
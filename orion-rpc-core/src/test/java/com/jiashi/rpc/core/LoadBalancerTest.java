package com.jiashi.rpc.core.test;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.core.loadbalancer.LoadBalancer;
import com.jiashi.rpc.core.loadbalancer.RandomLoadBalancer;
import com.jiashi.rpc.core.loadbalancer.RoundRobinLoadBalancer;
import com.jiashi.rpc.core.registry.ServiceInstance;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancerTest {

    public static void main(String[] args) {
        // 模拟服务列表(假装从ZK取到了3台机器)
        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(ServiceInstance.builder().host("127.0.0.1").port(8081).serviceName("UserService").build());
        instances.add(ServiceInstance.builder().host("127.0.0.1").port(8082).serviceName("UserService").build());
        instances.add(ServiceInstance.builder().host("127.0.0.1").port(8083).serviceName("UserService").build());

        // 构造一个空的 Request (因为轮询和随机都不看 Request，所以传空没关系)
        RpcRequest request = new RpcRequest();

        System.out.println("========== 测试轮询 (RoundRobin) ==========");
        testRoundRobin(instances, request);

        System.out.println("\n========== 测试随机 (Random) ==========");
        testRandom(instances, request);
    }

    public static void testRoundRobin(List<ServiceInstance> instances, RpcRequest request) {
        // 创建轮询策略
        LoadBalancer roundRobin = new RoundRobinLoadBalancer();

        // 预期结果：应该严格按照 8081 -> 8082 -> 8083 -> 8081... 循环
        for (int i = 0; i < 6; i++) {
            ServiceInstance selected = roundRobin.select(instances, request);
            System.out.println("第 " + (i + 1) + " 次调用选中: " + selected.getPort());
        }
    }

    public static void testRandom(List<ServiceInstance> instances, RpcRequest request) {
        // 创建随机策略
        LoadBalancer random = new RandomLoadBalancer();

        // 预期结果：完全没有规律，可能重复，可能跳跃
        for (int i = 0; i < 6; i++) {
            ServiceInstance selected = random.select(instances, request);
            System.out.println("第 " + (i + 1) + " 次调用选中: " + selected.getPort());
        }
    }
}
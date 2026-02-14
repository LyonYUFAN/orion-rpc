package com.jiashi.rpc.core.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务实例类
 * 代表注册中心里的一个具体机器节点
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceInstance {
    /**
     * 服务名称 (例如: UserService)
     */
    private String serviceName;

    /**
     * 主机地址 (IP)
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    // 以后你可以加更多字段，比如：
    // private int weight; // 权重
    // private String group; // 分组
}
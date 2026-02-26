package com.jiashi.rpc.core;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.core.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component // 交给 Spring 管理
public class ClientTestController {

    @RpcReference // 触发 RpcSpringPostProcessor 注入代理对象
    private HelloService helloService;

    public void testRpcCall() {
        System.out.println("准备发起 RPC 远程调用...");
        String result = helloService.hello("Jiashi");
        System.out.println("RPC 调用结果: " + result);
    }
}
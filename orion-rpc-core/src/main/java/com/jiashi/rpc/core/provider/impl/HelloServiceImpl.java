package com.jiashi.rpc.core.provider.impl;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.core.annotation.RpcService;

@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String msg) {
        return "我是通过RPC调用过的真正的结果" + msg;
    }
}


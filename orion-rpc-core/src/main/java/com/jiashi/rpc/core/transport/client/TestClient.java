package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.api.HelloService;
import com.jiashi.rpc.core.transport.client.proxy.RpcClientProxy;

public class TestClient {

    public static void main(String[] args) {

        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);

        nettyClient.connect();

        try {
            RpcClientProxy proxy = new RpcClientProxy(nettyClient);
            HelloService helloService = proxy.getProxy(HelloService.class);

            String res = helloService.hello("OrionRPC");
            System.out.println("服务端返回结果：" + res);

            String res2 = helloService.hello("World");
            System.out.println("服务端返回结果2：" + res2);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nettyClient.close();
        }
    }
}
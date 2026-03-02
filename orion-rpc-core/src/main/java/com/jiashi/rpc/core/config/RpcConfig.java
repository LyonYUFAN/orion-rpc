package com.jiashi.rpc.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RpcConfig {
    private String serverHost = "127.0.0.1";
    private int serverPort = 7777;
    private String zkAddress = "127.0.0.1:2181";
}


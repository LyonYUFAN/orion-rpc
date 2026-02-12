package com.jiashi.rpc.core.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {

    SERVICE_REGISTER_FAILED("注册服务失败"),
    SERVICE_NOT_FOUND("没有找到指定的服务"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    INTERFACE_NOT_FOUND("没有找到对应的接口"),
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误！请求和返回的响应不匹配");

    private final String message;
}
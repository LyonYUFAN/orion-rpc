package com.jiashi.rpc.common.entity;

/**
 * RPC 请求实体类
 * 客户端发送给服务端的消息格式
 */
public class RpcRequest {

    // 请求号 (用来匹配响应)
    private String requestId;
    // 调用的接口名 (例如: com.jiashi.rpc.api.HelloService)
    private String interfaceName;
    // 调用的方法名 (例如: hello)
    private String methodName;
    // 参数类型列表 (用来处理方法重载)
    private Class<?>[] paramTypes;
    // 参数值列表
    private Object[] parameters;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Class<?>[] getParamTypes() { return paramTypes; }
    public void setParamTypes(Class<?>[] paramTypes) { this.paramTypes = paramTypes; }

    public Object[] getParameters() { return parameters; }
    public void setParameters(Object[] parameters) { this.parameters = parameters; }

    //  (方便调试打印) ---
    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
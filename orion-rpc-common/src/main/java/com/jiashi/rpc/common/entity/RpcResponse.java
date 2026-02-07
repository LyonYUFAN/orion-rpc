package com.jiashi.rpc.common.entity;

/**
 * RPC 响应实体类
 * 服务端回复给客户端的消息格式
 */
public class RpcResponse {

    // 对应的请求号
    private Integer requestId;
    // 状态码 (200=成功, 500=失败)
    private Integer code;
    // 错误信息 (如果失败了，这里放报错内容)
    private String msg;
    // 返回的数据结果 (成功的话，结果在这个里面)
    private Object data;

    // --- 快速构建成功/失败结果的静态方法 (方便后面写代码) ---
    public static RpcResponse success(Object data) {
        RpcResponse response = new RpcResponse();
        response.setCode(200);
        response.setMsg("success");
        response.setData(data);
        return response;
    }

    public static RpcResponse fail(String msg) {
        RpcResponse response = new RpcResponse();
        response.setCode(500);
        response.setMsg(msg);
        return response;
    }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
package com.jiashi.rpc.core.transport.server.handler;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.entity.RpcResponse;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.registry.LocalRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 业务处理 Handler
 * 负责读取客户端发来的 RpcMessage，并返回响应
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage> {

    //channelRead0在数据已经被解码成一个完整的Java对象，且类型匹配的时触发
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {
        log.info("服务端收到消息:{}",rpcMessage);
        if(rpcMessage.getMessageType() == MessageType.REQUEST.getCode()){
            // 请求
            // TODO: 这里以后会改成“反射调用真实服务”
            RpcRequest request = (RpcRequest)rpcMessage.getData();

            // 构造响应体 (RpcResponse)
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcMessage.getRequestId()); // ID 必须对应

            try{
                Object serviceImpl = LocalRegistry.getService(request.getInterfaceName());
                if (serviceImpl == null) {
                    throw new RuntimeException("服务未找到: " + request.getInterfaceName());
                }

                // 反射
                Method method = serviceImpl.getClass().getMethod(
                        request.getMethodName(),
                        request.getParamTypes()
                );
                Object result = method.invoke(serviceImpl, request.getParameters());

                response.setCode(200);
                response.setMsg("success");
                response.setData(result);

            }catch (Exception e){
                log.error("服务调用出错", e);
                response.setCode(500);
                response.setMsg("服务调用出错: " + e.getMessage());
                response.setData(null);
            }

            // 构造协议包 (RpcMessage)
            RpcMessage responseMsg = new RpcMessage();
            responseMsg.setCodec(rpcMessage.getCodec()); // 保持编码方式一致
            responseMsg.setCompress(rpcMessage.getCompress());
            responseMsg.setMessageType(MessageType.RESPONSE.getCode()); // 设置为响应类型
            responseMsg.setRequestId(rpcMessage.getRequestId());
            responseMsg.setData(response);

            channelHandlerContext.writeAndFlush(responseMsg);
            log.info("服务端响应发送完毕");
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}


package com.jiashi.rpc.core.transport.client;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.enums.MessageType;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.core.codec.RpcMessageDecoder;
import com.jiashi.rpc.core.codec.RpcMessageEncoder;
import com.jiashi.rpc.core.protocol.RpcMessage;
import com.jiashi.rpc.core.provider.impl.HelloServiceImpl;
import com.jiashi.rpc.core.transport.client.handler.RpcResponseHandler;
import com.jiashi.rpc.core.transport.client.initializer.RpcResponseInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient {

    public void start(String host, int port) {
        start0(host, port);
    }

    public void start0(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class) // 客户端用 SocketChannel
                    .handler(new RpcResponseInitializer());

            ChannelFuture future = bootstrap.connect(host, port).sync();
            log.info("客户端连接成功！");

            // 2. 构造一个假请求 (模拟去调用 HelloService 的 sayHello 方法)
            RpcRequest request = new RpcRequest();
            request.setRequestId(12345);
            request.setInterfaceName(HelloServiceImpl.class.getName());
            request.setMethodName("hello");
            request.setParameters(new Object[]{"hello OrionRpc"});
            request.setParamTypes(new Class[]{String.class});

            // 3. 包装成协议消息
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setCodec(SerializationType.PROTOSTUFF.getCode()); // 记得设置序列化方式
            rpcMessage.setCompress((byte) 0);
            rpcMessage.setMessageType(MessageType.REQUEST.getCode()); // 类型是 REQUEST
            rpcMessage.setRequestId(12345);
            rpcMessage.setData(request);

            // 4. 发送数据！
            future.channel().writeAndFlush(rpcMessage);

            // 5. 等待关闭 (这一步是为了让客户端不要发完立刻退出，等着接收响应)
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("客户端报错", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyClient().start("127.0.0.1", 8088);
    }
}
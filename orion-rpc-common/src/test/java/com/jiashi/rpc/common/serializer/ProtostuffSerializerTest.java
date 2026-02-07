package com.jiashi.rpc.common.serializer;

import com.jiashi.rpc.common.entity.RpcRequest;
import com.jiashi.rpc.common.serializer.impl.ProtostuffSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * 单元测试：验证 Protostuff 序列化与反序列化的正确性
 */
public class ProtostuffSerializerTest {

    @Test
    @DisplayName("测试 RpcRequest 对象的完整序列化流程")
    public void testSerializer() {
        // 1. 准备阶段 (Arrange)
        Serializer serializer = new ProtostuffSerializer();

        // 构造一个模拟的 RPC 请求对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(111);
        rpcRequest.setInterfaceName("com.jiashi.rpc.api.HelloService");
        rpcRequest.setMethodName("hello");
        rpcRequest.setParameters(new Object[]{"yufan", 666}); // 测试多种类型的参数
        rpcRequest.setParamTypes(new Class[]{String.class, Integer.class});

        // 2. 执行阶段 (Act)
        // 序列化
        byte[] bytes = serializer.serialize(rpcRequest);
        // 反序列化
        RpcRequest decodedRequest = serializer.deserialize(bytes, RpcRequest.class);

        // 3. 断言阶段 (Assert) - 这是“标准”的核心

        // 验证结果不为空
        Assertions.assertNotNull(decodedRequest, "反序列化后的对象不应为空");

        // 验证基础属性一致
        Assertions.assertEquals(rpcRequest.getRequestId(), decodedRequest.getRequestId(), "请求ID应当一致");
        Assertions.assertEquals(rpcRequest.getInterfaceName(), decodedRequest.getInterfaceName(), "接口名应当一致");
        Assertions.assertEquals(rpcRequest.getMethodName(), decodedRequest.getMethodName(), "方法名应当一致");

        // 验证数组内容一致 (注意：数组比较不能直接用 assertEquals，要用 assertArrayEquals)
        Assertions.assertArrayEquals(rpcRequest.getParameters(), decodedRequest.getParameters(), "参数值列表应当一致");
        Assertions.assertArrayEquals(rpcRequest.getParamTypes(), decodedRequest.getParamTypes(), "参数类型列表应当一致");

        // 如果代码能运行到这里，说明所有测试全部通过
        System.out.println("✅ [ProtostuffSerializerTest] 单元测试通过！");
    }
}
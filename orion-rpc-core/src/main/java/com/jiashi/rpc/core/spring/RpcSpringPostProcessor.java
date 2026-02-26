package com.jiashi.rpc.core.spring;

import com.jiashi.rpc.core.annotation.RpcReference;
import com.jiashi.rpc.core.annotation.RpcService;
import com.jiashi.rpc.core.provider.LocalRegistry;
import com.jiashi.rpc.core.registry.ServiceInstance;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import com.jiashi.rpc.core.transport.client.proxy.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

@Component
@Slf4j
public class RpcSpringPostProcessor implements BeanPostProcessor {

    private final ServiceRegistry serviceRegistry; // 注册中心
    private final RpcClientProxy rpcClientProxy;   // 代理类

    public RpcSpringPostProcessor(ServiceRegistry serviceRegistry, RpcClientProxy rpcClientProxy) {
        this.serviceRegistry = serviceRegistry;
        this.rpcClientProxy = rpcClientProxy;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                Class<?> interfaceClass = field.getType();
                Object proxyObject = rpcClientProxy.getProxy(interfaceClass);
                // 按照Java的基本面向对象原则,外部类是绝对不允许直接给其他类的private变量赋值的
                // 执行了这行代码后,紧接着的field.set(bean,proxyObject);才能成功执行。
                // 它能把动态代理对象强行“塞”进那个原本是 private 的变量里。
                // Spring框架底层在进行@Autowired依赖注入时用的也是一模一样的方法。
                field.setAccessible(true);
                try {
                    field.set(bean, proxyObject);
                    log.info("成功为字段 {} 注入 RPC 代理对象", field.getName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入 RPC 代理失败", e);
                }
            }
        }
        return bean;
    }

    // 扫描@RpcService并注册到Zookeeper
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        if (targetClass.isAnnotationPresent(RpcService.class)) {
            // 获取类实现的接口，通常RPC暴露的是接口
            Class<?>[] interfaces = targetClass.getInterfaces();
            if (interfaces.length == 0) {
                throw new RuntimeException("标注了@RpcService的类必须实现至少一个接口");
            }
            // TODO: 这里默认的拿第一个实现接口不太严谨,遇到实现了多个接口会有风险
            String serviceName = interfaces[0].getCanonicalName();
            // TODO: 这里先写死
            serviceRegistry.registerService(serviceName,new InetSocketAddress("127.0.0.1",7777));
            LocalRegistry.register(serviceName,bean);
            log.info("发现 RPC 服务提供者: {}, 已准备好注册逻辑", serviceName);
        }
        return bean; // 同样必须返回 bean
    }
}


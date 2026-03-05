package com.jiashi.rpc.spring.boot.autoconfigure.processor;

import com.jiashi.rpc.spring.boot.autoconfigure.annotation.RpcReference;
import com.jiashi.rpc.spring.boot.autoconfigure.annotation.RpcService;
import com.jiashi.rpc.core.provider.LocalRegistry;
import com.jiashi.rpc.core.registry.ServiceRegistry;
import com.jiashi.rpc.core.transport.client.proxy.RpcClientProxy;
import com.jiashi.rpc.spring.boot.autoconfigure.config.properties.RpcProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Component
@Slf4j
public class RpcSpringPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    // 事实:实现ApplicationContextAware获取上下文，取代构造器强注入
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 事实：ReflectionUtils.doWithFields 能够递归扫描该类及其所有父类的字段
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                Class<?> interfaceClass = field.getType();
                // 在需要用的时候再去 context 里面懒获取，避免代理类及其依赖被 Spring 提前初始化
                RpcClientProxy rpcClientProxy = applicationContext.getBean(RpcClientProxy.class);
                Object proxyObject = rpcClientProxy.getProxy(interfaceClass);
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, proxyObject);
                log.info("成功为 Bean [{}] 的字段 [{}] 注入 RPC 代理对象", beanName, field.getName());
            }
        });
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // AnnotationUtils.findAnnotation 能自动穿透 CGLIB 代理的子类，查找到真实类上的自定义注解
        RpcService rpcService = AnnotationUtils.findAnnotation(bean.getClass(), RpcService.class);
        if (rpcService != null) {
            // 事实：如果是被 AOP 代理的类，必须获取真实的目标 Class 才能拿到用户手写的接口信息
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Class<?>[] interfaces = targetClass.getInterfaces();
            if (interfaces.length == 0) {
                throw new RuntimeException("标注了 @RpcService 的类必须实现至少一个接口");
            }
            // 默认取第一个接口作为服务名暴露
            String serviceName = interfaces[0].getCanonicalName();
            // 同样采用懒获取注册中心和配置
            ServiceRegistry serviceRegistry = applicationContext.getBean(ServiceRegistry.class);
            RpcProperties rpcProperties = applicationContext.getBean(RpcProperties.class);
            try {
                // 动态获取本机 IP。生产环境中，IP 和 Port 通常由 RpcProperties 提供
                String host = rpcProperties.getServerHost();
                int port = rpcProperties.getServerPort();
                serviceRegistry.registerService(serviceName, new InetSocketAddress(host, port));
                LocalRegistry.register(serviceName, bean);
                log.info("发现并注册 RPC 服务提供者: {}, 暴露地址: {}:{}", serviceName, host, port);
            } catch (Exception e) {
                log.error("RPC 服务注册失败: {}", serviceName, e);
                throw new RuntimeException("RPC 服务注册失败", e);
            }
        }
        return bean;
    }
}
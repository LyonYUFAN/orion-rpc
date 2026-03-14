# 🚀 OrionRPC - 高性能分布式 RPC 框架

![Java](https://img.shields.io/badge/Java-11%2B-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-brightgreen.svg) ![Netty](https://img.shields.io/badge/Netty-4.1-green.svg) ![Zookeeper](https://img.shields.io/badge/Zookeeper-3.4%2B-yellow.svg)

OrionRPC 是一个基于 Java 纯手写的高性能、高可扩展的分布式远程过程调用（RPC）框架。
本项目深度剖析了底层中间件的架构设计，内置自定义 SPI 机制、全异步网络通信、自定义二进制协议，并提供对 Spring 生态的开箱即用支持。

## 🛠️ 技术栈 (Tech Stack)

本项目坚持轻量化原则，核心依赖精简如下：
* **核心开发：** Java 11+
* **网络通信：** Netty 4.1.x
* **服务发现：** Zookeeper 3.4+ & Apache Curator
* **框架集成：** Spring Boot 2.x

## ✨ 核心架构与功能 (Core Features)

* **微内核架构 (SPI 机制)：** 纯手写类似 Dubbo 的 `@SPI` 注解与 `ExtensionLoader`，核心组件（序列化、负载均衡、注册中心）均实现完全解耦，支持动态拔插和自定义扩展。
* **原生 Spring 生态支持：** 提供自定义 `orion-rpc-spring-boot-starter`，通过 `@RpcService` 和 `@RpcReference` 注解实现业务代码零侵入，开箱即用。
* **全异步非阻塞调用：** 摒弃传统的同步阻塞等待，底层基于 `CompletableFuture` 与 Netty `Channel` 缓存池，实现真正的高并发异步通信。
* **自定义通信协议：** 设计了包含魔数、版本、序列化类型等元数据的严格二进制协议，配合自定义编解码器，从根本上解决 TCP 粘包/拆包问题。
* **丰富的服务治理：** * 内置 Kryo、Protostuff(默认)、JSON 三种序列化算法。
    *  内置 Random、RoundRobin、ConsistentHash 三种负载均衡策略。

---
## 💻 前置要求与安装 (Prerequisites & Install)

**环境要求：**
* [JDK 11+](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Maven 3.6+](https://maven.apache.org/download.cgi)
* [Zookeeper](https://zookeeper.apache.org/releases.html) (本地或远程运行均可，默认使用 `127.0.0.1:2181`)

**克隆与本地构建：**

打开终端，执行以下命令将 OrionRPC 安装到你的本地 Maven 仓库：

```bash
# 克隆仓库
git clone [https://github.com/your-username/orion-rpc.git](https://github.com/your-username/orion-rpc.git)
cd orion-rpc

# 编译并安装到本地 Maven 仓库 (跳过测试)
mvn clean install -DskipTests
```

---

## ⚡ 快速上手

OrionRPC 提供了两种接入方式，你可以根据项目类型自由选择。在开始前，请确保服务提供者和消费者都依赖了定义好的公共接口（如 `HelloService`）。

### 方式一：使用 Spring Boot (推荐)
通过引入 `orion-rpc-spring-boot-starter`，实现真正的零侵入。

**1. 引入依赖**
```xml
<dependency>
    <groupId>com.jiashi.rpc</groupId>
    <artifactId>orion-rpc-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**2. 配置 application.yml**
```yaml
orion:
  rpc:
    server-port: 9999              # 当前服务端暴露的 RPC 端口
    registry-address: 127.0.0.1:2181 # Zookeeper 地址
```

**3. 暴露服务 (Provider)**
使用 `@RpcService` 标注实现类，框架会自动将其注册到 Zookeeper：
```java
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name + ", response from Spring Boot Provider!";
    }
}
```

**4. 消费服务 (Consumer)**
使用 `@RpcReference` 注入远程接口，支持指定负载均衡策略：
```java
@RestController
public class HelloController {

    @RpcReference(loadBalancer = "consistentHash") 
    private HelloService helloService;

    @GetMapping("/hello")
    public String testRpc(@RequestParam String name) {
        return helloService.sayHello(name); 
    }
}
```

### 方式二：原生 Java 启动
不依赖 Spring 生态，核心包同样支持通过 API 直接调用。

**1. 引入核心依赖**
```xml
<dependency>
    <groupId>com.jiashi.rpc</groupId>
    <artifactId>orion-rpc-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**2. 启动服务端**
```java
RpcConfig config = new RpcConfig();
config.setServerHost("127.0.0.1");
config.setServerPort(9999);
config.setRegistryAddress("127.0.0.1:2181");

NettyServer server = new NettyServer(config);
server.start(); 
```

**3. 发起客户端调用**
```java
RpcClientProxy proxy = new RpcClientProxy();
HelloService helloService = proxy.getProxy(HelloService.class);
String result = helloService.sayHello("Native Java Developer");
System.out.println(result);
```

---

## 📊 性能表现

*测试环境：个人 PC 常规供电状态下，单机双进程（Provider/Consumer）*
*测试条件：50 并发线程，总请求量 50,000 次。固定采用 Random (随机) 负载均衡策略*

| 序列化协议               | 负载均衡策略 | 平均响应时间 (RT) | 极限吞吐量 (QPS) |
| :------------------ | :----- | :---------- | :---------- |
| **Kryo**            | Random | `3.36 ms`   | `14,836`    |
| **Protostuff** (默认) | Random | `3.43 ms`   | `14,547`    |
| **JSON**            | Random | `3.85 ms`   | `12,973`    |

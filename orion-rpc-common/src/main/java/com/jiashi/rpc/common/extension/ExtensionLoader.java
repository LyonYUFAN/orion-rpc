package com.jiashi.rpc.common.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoader<T> {

    private static final String EXTENSION_DIR="META-INF/extensions/";

    private static final ConcurrentHashMap<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS=new ConcurrentHashMap<Class<?>,ExtensionLoader<?>>();

    private final ConcurrentHashMap<String, Holder<Object>> cacheInstance=new ConcurrentHashMap<>();

    private final Holder<Map<String,Class<?>>> cacheClasses = new Holder<>();

    // 这个扩展器具体负责的类型
    private final Class<T> type;

    public ExtensionLoader(Class<T> type){
        this.type=type;
    }

    // 获取某个接口类型的全局唯一扩展器
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type == null){
            throw new IllegalArgumentException("type is null");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("type must be interface");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 从全局唯一的EXTENSION_LOADERS中找到对应的扩展器
        ExtensionLoader<S> extensionLoader=(ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        if(extensionLoader == null){
            // 利用ConcurrentHashMap的putIfAbsent原子操作放入新创建的管家，防止并发覆盖
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 根据扩展名获取真正的实现类对象
     * @param name 配置文件中的 key，例如 "protostuff"
     * @return
     */
    public T getExtension(String name){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("name is null or empty");
        }
        Holder<Object> holder = cacheInstance.get(name);
        if(holder == null){
            cacheInstance.putIfAbsent(name,new Holder<>());
            holder = cacheInstance.get(name);
        }
        // 双重检查锁
        Object instance = holder.getValue();
        if(instance == null){
            // 加锁保证只有一个线程能够进入
            synchronized (holder){
                instance = holder.getValue();
                if(instance == null){
                    instance = createExtension(name);
                    holder.setValue(instance);
                }
            }
        }
        return (T) instance;
    }

    private T createExtension(String name){
        // 需要通过name拿到对应的class字节码
        Class<?> clazz = getExtensionClasses().get(name);
        if(clazz == null){
            throw new IllegalArgumentException("Extension class not found");
        }
        try{
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Error creating extension {}: {}", name, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 懒加载读取配置文件：获取该接口下所有的 Key-Value 映射
     */
    private Map<String,Class<?>> getExtensionClasses(){
        Map<String, Class<?>> classes = cacheClasses.getValue();
        if(classes == null){
            synchronized (cacheClasses){
                classes = cacheClasses.getValue();
                if(classes == null){
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cacheClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String, Class<?>> classes){
        String filename = EXTENSION_DIR+type.getName();
        System.out.println(">>> 准备扫描的物理路径是: " + filename);
        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = classLoader.getResources(filename);
            if(urls!=null){
                while(urls.hasMoreElements()){
                    URL url = urls.nextElement();
                    loadResource(classes,classLoader,url);
                }
            }
        } catch (Exception e) {
            log.error("Load extension directory error", e);
        }
    }

    /**
     * 解析具体文件的内容
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 1. 处理注释和空白
                final int ci = line.indexOf('#');
                if (ci >= 0) line = line.substring(0, ci);
                line = line.trim();

                if (line.length() > 0) {
                    // 2. 按照 key=value 解析
                    int i = line.indexOf('=');
                    if (i > 0) {
                        String name = line.substring(0, i).trim();
                        String clazzName = line.substring(i + 1).trim();
                        if (!name.isEmpty() && !clazzName.isEmpty()) {
                            // 3. 将字符串类名装载为 Class 对象，存入“桶”中
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Load resource file error: {}", resourceUrl, e);
        }
    }
}

class Holder<T> {
    private volatile T value;
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
}


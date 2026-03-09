package com.jiashi.rpc.common.serializer.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiashi.rpc.common.enums.SerializationType;
import com.jiashi.rpc.common.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JSON 序列化器实现
 */
@Slf4j
public class JsonSerializer implements Serializer {

    // 事实：Jackson 的 ObjectMapper 是线程安全的，建议重用
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte getCode() {
        // 对应你之前定义的 SerializationType.JSON((byte) 1)
        return (byte) 1;
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败: {}", e.getMessage());
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("JSON 反序列化失败: {}", e.getMessage());
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}
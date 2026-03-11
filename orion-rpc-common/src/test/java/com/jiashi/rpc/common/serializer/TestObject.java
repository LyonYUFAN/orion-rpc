package com.jiashi.rpc.common.serializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestObject implements Serializable {
    private String name;
    private int age;
    private List<String> skills;
    private Map<String, String> metadata;
}
package com.jiashi.rpc.core.protocol;

public class ProtocolConstants {

    public static final int HEARTBEAT_ID = 0; // 专门留给心跳的 ID
    public static final byte[] MAGIC_NUMBER = {(byte) 'o', (byte) 'r', (byte) 'i', (byte) 'o'};
    public static final byte VERSION = 1;
    // 头部长度 (魔数4 + 版本1 + 序列化1 + 类型1 + 状态1 + ID 4 + 长度4)
    public static final int HEAD_LENGTH = 16;

}


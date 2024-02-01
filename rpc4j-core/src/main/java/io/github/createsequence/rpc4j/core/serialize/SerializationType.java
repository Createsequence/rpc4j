package io.github.createsequence.rpc4j.core.serialize;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 序列化类型
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public enum SerializationType {

    /**
     * 基于fastjson的json序列化
     */
    FASTJSON((byte) 0x01, "fastjson"),

    ;

    /**
     * 编码映射缓存
     */
    public static final Map<Byte, SerializationType> LOOKUP = Map.copyOf(Arrays.stream(
        SerializationType.values()).collect(Collectors.toMap(SerializationType::getCode, Function.identity())
    ));

    private final byte code;
    private final String name;
}

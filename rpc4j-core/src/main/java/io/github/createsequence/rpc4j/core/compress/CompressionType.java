package io.github.createsequence.rpc4j.core.compress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 压缩器类型
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public enum CompressionType {

    /**
     * 无
     */
    NONE((byte) 0x01, "none"),

    ;

    /**
     * 编码映射缓存
     */
    public static final Map<Byte, CompressionType> LOOKUP = Map.copyOf(Arrays.stream(
        CompressionType.values()).collect(Collectors.toMap(CompressionType::getCode, Function.identity())
    ));

    private final byte code;
    private final String name;
}

package io.github.createsequence.rpc4j.core.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public enum ResponseStatus {

    /**
     * 响应成功
     */
    SUCCESS((byte)0x01, "正常"),

    /**
     * 响应失败
     */
    FAILURE((byte)0x02, "失败"),

    ;

    /**
     * 编码映射缓存
     */
    public static final Map<Byte, ResponseStatus> LOOKUP = Map.copyOf(Arrays.stream(
        ResponseStatus.values()).collect(Collectors.toMap(ResponseStatus::getCode, Function.identity())
    ));

    private final byte code;
    private final String name;
}

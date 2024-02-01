package io.github.createsequence.rpc4j.core.transport;

import io.github.createsequence.rpc4j.core.support.Request;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 协议版本号
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public enum ProtocolVersion {

    /**
     * 正常的RPC请求，通过{@link Packet#getData()}将获得{@link Request}
     */
    V1((byte)0x01, "请求"),

    ;

    /**
     * 编码映射缓存
     */
    public static final Map<Byte, ProtocolVersion> LOOKUP = Map.copyOf(Arrays.stream(
        ProtocolVersion.values()).collect(Collectors.toMap(ProtocolVersion::getCode, Function.identity())
    ));

    private final byte code;
    private final String name;
}

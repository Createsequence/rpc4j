package io.github.createsequence.rpc4j.core.transport;

import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据包类型
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public enum PacketType {

    /**
     * 正常的RPC请求，通过{@link Packet#getData()}将获得{@link Request}
     */
    RPC_REQUEST((byte)0x01, "请求"),

    /**
     * 正常的RPC请求，通过{@link Packet#getData()}将获得{@link Response}
     */
    RPC_RESPONSE((byte)0x02, "响应"),

    ;

    /**
     * 编码映射缓存
     */
    public static final Map<Byte, PacketType> LOOKUP = Map.copyOf(Arrays.stream(
        PacketType.values()).collect(Collectors.toMap(PacketType::getCode, Function.identity())
    ));

    private final byte code;
    private final String name;
}

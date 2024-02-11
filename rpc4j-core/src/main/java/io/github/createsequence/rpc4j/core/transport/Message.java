package io.github.createsequence.rpc4j.core.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RPC消息体
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class Message<T> {

    /**
     * 协议版本
     */
    private final byte version;

    /**
     * 消息类型
     */
    private final byte messageType;

    /**
     * 序列化方式
     */
    private final byte serializationType;

    /**
     * 压缩方式
     */
    private final byte compressType;

    /**
     * 响应
     */
    private final T payload;

    @Override
    public String toString() {
        return getHeader().toString();
    }

    public Header getHeader() {
        return new Header(
            Rpc4jProtocol.Version.LOOKUP.get(version),
            Rpc4jProtocol.MessageType.LOOKUP.get(messageType),
            Rpc4jProtocol.SerializationType.LOOKUP.get(serializationType),
            Rpc4jProtocol.CompressionType.LOOKUP.get(compressType)
        );
    }

    public record Header(
        Rpc4jProtocol.Version version,
        Rpc4jProtocol.MessageType messageType,
        Rpc4jProtocol.SerializationType serializationType,
        Rpc4jProtocol.CompressionType compressType
    ) {}
}

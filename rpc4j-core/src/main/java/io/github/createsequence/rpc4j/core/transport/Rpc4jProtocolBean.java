package io.github.createsequence.rpc4j.core.transport;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * RPC4J协议头
 *
 * @author huangchengxing
 */
@Accessors(chain = true)
@Setter
@Getter
@NoArgsConstructor
public class Rpc4jProtocolBean {
    /**
     * 协议版本号
     */
    private byte protocolVersion;

    /**
     * 序列化方式
     */
    private byte serializationType;

    /**
     * 压缩方式
     */
    private byte compressionType;
}

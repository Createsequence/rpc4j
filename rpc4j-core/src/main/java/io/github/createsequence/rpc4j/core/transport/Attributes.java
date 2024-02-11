package io.github.createsequence.rpc4j.core.transport;

/**
 * @author huangchengxing
 */
public interface Attributes {

    /**
     * 用于在客户端向服务端发起请求的地址
     */
    String REMOTE_ADDRESS = "remoteAddress";

    /**
     * 用于在服务端接收到请求时记录请求ID
     */
    String REQUEST_ID = "requestId";

    /**
     * 请求超时时间
     */
    String REQUEST_TIMEOUT = "requestTimeout";

    /**
     * 请求超时时间单位
     */
    String REQUEST_TIMEOUT_UNIT = "requestTimeoutUnit";

    /**
     * 请求协议版本
     */
    String REQUEST_PROTOCOL_VERSION = "requestDefaultValue";

    /**
     * 压缩方式
     */
    String COMPRESSION_TYPE = "compressionType";

    /**
     * 序列化类型
     */
    String SERIALIZATION_TYPE = "serializationType";

    /**
     * 负载均衡策略
     */
    String LOAD_BALANCE_STRATEGY = "RandomLoadBalance";
}

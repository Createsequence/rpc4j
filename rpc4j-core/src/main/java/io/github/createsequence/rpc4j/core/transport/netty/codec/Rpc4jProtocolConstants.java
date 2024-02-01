package io.github.createsequence.rpc4j.core.transport.netty.codec;

import io.github.createsequence.rpc4j.core.compress.CompressionType;
import io.github.createsequence.rpc4j.core.serialize.SerializationType;
import io.github.createsequence.rpc4j.core.transport.PacketType;
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.github.createsequence.rpc4j.core.transport.ResponseStatus;

/**
 * 自定义协议常量，从头部开始计算共分为五个部分：
 * <ol>
 *     <li>魔数（5B）：用于校验是否是为rpc4j的自定义协议；</li>
 *     <li>协议版本号（1B）: 参见{@link ProtocolVersion}</li>
 *     <li>序列化算法（1B）：用于标识数据的序列化算法，参见{@link SerializationType}；</li>
 *     <li>压缩算法（1B）: 参见{@link CompressionType}</li>
 *     <li>报文类型（1B）：表示当前请求报文类型，参见{@link PacketType}；</li>
 *     <li>请求状态（1B）；参见{@link ResponseStatus}</li>
 *     <li>报文长度（4B）；</li>
 *     <li>请求数据；</li>
 * </ol>
 * 一个数据帧的最大为2M。
 *
 * @author huangchengxing
 * @see <a href="https://learn.lianglianglee.com/%e4%b8%93%e6%a0%8f/Netty%20%e6%a0%b8%e5%bf%83%e5%8e%9f%e7%90%86%e5%89%96%e6%9e%90%e4%b8%8e%20RPC%20%e5%ae%9e%e8%b7%b5-%e5%ae%8c/07%20%20%e6%8e%a5%e5%a4%b4%e6%9a%97%e8%af%ad%ef%bc%9a%e5%a6%82%e4%bd%95%e5%88%a9%e7%94%a8%20Netty%20%e5%ae%9e%e7%8e%b0%e8%87%aa%e5%ae%9a%e4%b9%89%e5%8d%8f%e8%ae%ae%e9%80%9a%e4%bf%a1%ef%bc%9f.md">如何利用 Netty 实现自定义协议通信？</a>
 */
interface Rpc4jProtocolConstants {

    /**
     * 魔数长度，五个字节，即{@code 'rpc4j'}
     */
    int SIZE_OF_MAGIC_NUMBER_FIELD = 5;

    /**
     * 版本号长度，一个字节
     */
    int SIZE_OF_VERSION_FIELD = 1;

    /**
     * 序列化算法，一个字节
     *
     * @see SerializationType#getCode()
     */
    int SIZE_OF_SERIALIZER_TYPE_FIELD = 1;

    /**
     * 压缩算法，一个字节
     *
     * @see CompressionType#getCode()
     */
    int SIZE_OF_COMPRESSION_TYPE_FIELD = 1;

    /**
     * 报文类型长度，一个字节
     *
     * @see PacketType#getCode()
     */
    int SIZE_OF_TYPE_FIELD = 1;

    /**
     * 请求状态，一个字节
     *
     * @see ResponseStatus#getCode()
     */
    int SIZE_OF_STATUS_FIELD = 1;

    /**
     * 报文头部总长度
     */
    int SIZE_OF_HEADER_FIELDS = SIZE_OF_MAGIC_NUMBER_FIELD + SIZE_OF_VERSION_FIELD + SIZE_OF_SERIALIZER_TYPE_FIELD + SIZE_OF_COMPRESSION_TYPE_FIELD + SIZE_OF_TYPE_FIELD + SIZE_OF_STATUS_FIELD;

    /**
     * 长度域，表示一个完整报文的长度，四个字节刚好为一个int
     */
    int SIZE_OF_LENGTH_FIELD = 4;

    /**
     * 数据包的最大长度，8M
     */
    int MAX_FRAME_LENGTH = 80 * 1024 * 1024;
}

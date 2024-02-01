package io.github.createsequence.rpc4j.core.transport.netty.codec;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.compress.CompressionType;
import io.github.createsequence.rpc4j.core.compress.Compressor;
import io.github.createsequence.rpc4j.core.serialize.SerializationType;
import io.github.createsequence.rpc4j.core.serialize.Serializer;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import io.github.createsequence.rpc4j.core.transport.PacketType;
import io.github.createsequence.rpc4j.core.transport.ResponseStatus;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocolBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

/**
 * Netty编码器，当客户端向发送请求，或服务端向客户端响应时，
 * 将使用该编码器将数据包编码为Rpc4j协议格式的{@link Rpc4jProtocolBean 数据包}。
 *
 * @author huangchengxing
 * @see Rpc4jProtocolConstants
 * @see <a href = "https://www.lilinchao.com/archives/2166.html">Netty进阶之长度域解码器</a>
 */
@RequiredArgsConstructor
public class Rpc4jNettyEncoder extends MessageToByteEncoder<Rpc4jProtocolBean> implements Rpc4jProtocolConstants {
    private static final byte[] MAGIC_NUMBER = new byte[]{'r', 'p', 'c', '4', 'j'};

    private final ComponentManager componentManager;

    @Override
    protected void encode(
        ChannelHandlerContext channelHandlerContext, Rpc4jProtocolBean target, ByteBuf byteBuf) {
        // 写入魔数
        byteBuf.writeBytes(MAGIC_NUMBER);
        // 写入版本号
        byteBuf.writeByte(target.getProtocolVersion());

        // 序列方式
        SerializationType serializationType = SerializationType.LOOKUP.get(target.getSerializationType());
        Asserts.isNotNull(serializationType, "未知的序列化方式：{}", target.getSerializationType());
        Serializer serializer = componentManager.getComponent(Serializer.class, serializationType.getName());
        byteBuf.writeByte(serializationType.getCode());

        // 压缩方式
        CompressionType compressionType = CompressionType.LOOKUP.get(target.getCompressionType());
        Asserts.isNotNull(compressionType, "未知的压缩方式：{}", target.getCompressionType());
        Compressor compressor = componentManager.getComponent(Compressor.class, compressionType.getName());
        byteBuf.writeByte(compressionType.getCode());

        // 消息类型
        byte packetType = switch (target) {
            case Request ignored -> PacketType.RPC_REQUEST.getCode();
            case Response ignored -> PacketType.RPC_RESPONSE.getCode();
            default -> throw new Rpc4jException("未知的消息类型: {}", target.getClass());
        };
        byteBuf.writeByte(packetType);
        // 响应状态
        byteBuf.writeByte(ResponseStatus.SUCCESS.getCode());
        // 序列化数据，并在计算数据包总长度后写入
        byte[] payload = serializer.serialize(target);
        payload = compressor.compress(payload);
        int packetLength = SIZE_OF_HEADER_FIELDS + SIZE_OF_LENGTH_FIELD + payload.length;
        byteBuf.writeInt(packetLength);
        byteBuf.writeBytes(payload);
    }
}

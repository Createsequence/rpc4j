package io.github.createsequence.rpc4j.core.transport.codec;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.compress.Compressor;
import io.github.createsequence.rpc4j.core.serialize.Serializer;
import io.github.createsequence.rpc4j.core.transport.Message;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

/**
 * Netty编码器，当客户端向发送请求，或服务端向客户端响应时，
 * 将使用该编码器将数据包编码为Rpc4j协议格式的数据包。
 *
 * @author huangchengxing
 * @see <a href = "https://www.lilinchao.com/archives/2166.html">Netty进阶之长度域解码器</a>
 */
@RequiredArgsConstructor
public class Rpc4jNettyEncoder
    extends MessageToByteEncoder<Message<Object>> implements Rpc4jProtocol {

    private static final byte[] MAGIC_NUMBER = new byte[] {'r', 'p', 'c', '4', 'j'};

    private final ComponentManager componentManager;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message<Object> target, ByteBuf byteBuf) {
        // 写入魔数
        byteBuf.writeBytes(MAGIC_NUMBER);

        Message.Header protocolHeader = target.getHeader();
        verifyHeader(target, protocolHeader);

        // 版本号
        byteBuf.writeByte(protocolHeader.version().getCode());
        // 序列方式
        Serializer serializer = componentManager.getComponent(Serializer.class, protocolHeader.serializationType().getName());
        byteBuf.writeByte(protocolHeader.serializationType().getCode());
        // 压缩方式
        Compressor compressor = componentManager.getComponent(Compressor.class, protocolHeader.compressType().getName());
        byteBuf.writeByte(protocolHeader.compressType().getCode());
        // 消息类型
        byteBuf.writeByte(protocolHeader.messageType().getCode());
        // 响应状态
        byteBuf.writeByte(Rpc4jProtocol.ResponseStatus.SUCCESS.getCode());
        // 序列化数据，并在计算数据包总长度后写入
        byte[] payload = serializer.serialize(target.getPayload());
        payload = compressor.compress(payload);
        int packetLength = SIZE_OF_HEADER_FIELDS + SIZE_OF_LENGTH_FIELD + payload.length;
        byteBuf.writeInt(packetLength);
        byteBuf.writeBytes(payload);
    }

    private static void verifyHeader(Message<?> message, Message.Header header) {
        Asserts.isNotNull(header.serializationType(), "未知的序列化方式：{}", message.getSerializationType());
        Asserts.isNotNull(header.compressType(), "未知的压缩方式：{}", message.getCompressType());
        Asserts.isNotNull(header.messageType(), "未知的消息类型：{}", message.getMessageType());
        Asserts.isNotNull(header.version(), "未知的协议版本：{}", message.getVersion());
    }
}

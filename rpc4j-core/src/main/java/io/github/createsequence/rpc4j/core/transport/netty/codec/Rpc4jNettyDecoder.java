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
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>Netty解码器，当客户端接受到服务端响应，或服务端接受到客户端请求时，
 * 将使用该解码器将数据包解码为Rpc4j协议格式的数据包。
 *
 * <p>该编码器基于Netty的{@link LengthFieldBasedFrameDecoder}实现，
 * 其构造函数中参数说明如下：
 * <ul>
 *     <li>{@code maxFrameLength}：每个数据帧的最大长度；</li>
 *     <li>{@code lengthFieldOffset}：报文从第几个字节开始是长度域，即从报文头部后第一个字节开始；</li>
 *     <li>{@code lengthFieldLength}：即报文长度字段占用的字节数，固定为{@link Rpc4jProtocolConstants#SIZE_OF_LENGTH_FIELD}；</li>
 *     <li>{@code lengthAdjustment}：由于解码后我们仅需要保留数据包，因此获得的报文长度需要减去报文头部以及长度域本的长度，剩下的即为数据长度；</li>
 *     <li>{@code initialBytesToStrip}：解码后，跳过头部和长度域，即仅保留数据；</li>
 * </ul>
 * 完成解码后，最终拦截器将会获得请求参数，根据数据包类型不同，将会反序列化为不同的对象：
 * <ul>
 *     <li>{@link Request}：服务端收到客户端请求时；</li>
 *     <li>{@link Response}：客户端收到服务的响应时；</li>
 * </ul>
 *
 * @author huangchengxing
 * @see Rpc4jProtocolConstants
 * @see <a href = "https://www.lilinchao.com/archives/2166.html">Netty进阶之长度域解码器</a>
 */
@Slf4j
public class Rpc4jNettyDecoder extends LengthFieldBasedFrameDecoder implements Rpc4jProtocolConstants {

    private static final byte[] MAGIC_NUMBER = new byte[]{'r', 'p', 'c', '4', 'j'};
    private static final int MINIMUM_SIZE_OF_MESSAGE = SIZE_OF_HEADER_FIELDS + SIZE_OF_LENGTH_FIELD;

    /**
     * 反序列化器
     */
    private final ComponentManager componentManager;

    /**
     * 版本号
     */
    @Getter
    private final ProtocolVersion supportVersion;

    /**
     * 创建一个Netty解码器
     *
     * @param componentManager 组件管理器
     */
    public Rpc4jNettyDecoder(ComponentManager componentManager, ProtocolVersion supportVersion) {
        super(
            MAX_FRAME_LENGTH,
            SIZE_OF_HEADER_FIELDS,
            SIZE_OF_LENGTH_FIELD,
            - SIZE_OF_HEADER_FIELDS - SIZE_OF_LENGTH_FIELD,
            0
        );
        this.componentManager = componentManager;
        this.supportVersion = supportVersion;
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     * be created.
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object result = super.decode(ctx, in);
        if ((result instanceof ByteBuf buf)
            && (buf.readableBytes() >= MINIMUM_SIZE_OF_MESSAGE)) {
            try {
                return doDecode(ctx, buf);
            } catch (Exception ex) {
                log.error("解码失败！", ex);
            } finally {
                buf.release();
            }
        }
        return result;
    }

    protected Object doDecode(ChannelHandlerContext ctx, ByteBuf in) {
        checkMagicNumber(in);
        checkVersion(in);

        // 获取报文头部数据
        byte serializerType = in.readByte();
        byte compressionType = in.readByte();
        byte packetType = in.readByte();
        byte responseStatus = in.readByte();
        int packetLength = in.readInt();

        // 解析请求参数
        int sizeOfData = packetLength - SIZE_OF_HEADER_FIELDS - SIZE_OF_LENGTH_FIELD;
        if (sizeOfData < 0) {
            return null;
        }
        byte[] data = new byte[sizeOfData];
        in.readBytes(data);
        return deserialize(packetType, serializerType, compressionType, data);
    }

    protected Object deserialize(byte packetType, byte serializerType, byte compressionType, byte[] data) {
        // 对原始数据进行解压缩
        Compressor compressor = componentManager.getComponent(Compressor.class, CompressionType.LOOKUP.get(compressionType).getName());
        data = compressor.decompress(data);
        // 对解压缩后的数据进行反序列化
        SerializationType type = SerializationType.LOOKUP.get(serializerType);
        Asserts.isNotNull(serializerType, "未知的序列化类型编码: [{}]", type);
        Serializer serializer = componentManager.getComponent(Serializer.class, type.getName());
        return switch (PacketType.LOOKUP.get(packetType)) {
            case RPC_REQUEST -> serializer.deserialize(data, Request.class);
            case RPC_RESPONSE -> serializer.deserialize(data, Response.class);
            default -> throw new Rpc4jException("不支持的数据包类型: [{}]", packetType);
        };
    }

    private void checkVersion(ByteBuf byteBuf) {
        byte expectVersion = supportVersion.getCode();
        byte targetVersion = byteBuf.readByte();
        Asserts.isEquals(expectVersion, targetVersion, "当前支持的协议版本为[{}]，但实际为[{}]", expectVersion, targetVersion);
    }

    private void checkMagicNumber(ByteBuf byteBuf) {
        byte[] magicNumber = new byte[SIZE_OF_MAGIC_NUMBER_FIELD];
        byteBuf.readBytes(magicNumber);
        for (int i = 0; i < SIZE_OF_MAGIC_NUMBER_FIELD; i++) {
            if (magicNumber[i] != MAGIC_NUMBER[i]) {
                throw new Rpc4jException("报文前五位魔数必须为[{}]，但实际为[{}]", List.of(MAGIC_NUMBER), List.of(magicNumber));
            }
        }
    }
}

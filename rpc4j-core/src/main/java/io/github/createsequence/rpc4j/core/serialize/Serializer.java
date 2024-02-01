package io.github.createsequence.rpc4j.core.serialize;

/**
 * 序列化器
 *
 * @author huangchengxing
 */
public interface Serializer {

    /**
     * 将对象序列化为字节数组
     *
     * @param target 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object target);

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @param targetType 目标类
     * @param <T> 类型
     * @return 对象
     */
    <T> T deserialize(byte[] bytes, Class<T> targetType);
}

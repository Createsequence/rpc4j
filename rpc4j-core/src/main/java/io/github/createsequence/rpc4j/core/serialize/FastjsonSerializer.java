package io.github.createsequence.rpc4j.core.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;

/**
 * @author huangchengxing
 */
public class FastjsonSerializer implements Serializer {
    /**
     * 将对象序列化为字节数组
     *
     * @param target 要序列化的对象
     * @return 字节数组
     */
    @Override
    public byte[] serialize(Object target) {
        return JSON.toJSONBytes(target);
    }

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes      字节数组
     * @param targetType 目标类
     * @return 对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetType) {
        return JSON.parseObject(bytes, targetType, JSONReader.Feature.SupportClassForName);
    }
}

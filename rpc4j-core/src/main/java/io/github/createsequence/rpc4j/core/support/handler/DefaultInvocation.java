package io.github.createsequence.rpc4j.core.support.handler;

import io.github.createsequence.rpc4j.core.transport.RemoteAddress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 方法调用
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class DefaultInvocation implements RpcInvocation {

    private final List<RemoteAddress> remoteAddresses;
    private final String targetName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] arguments;
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 获取属性
     *
     * @param name 属性名
     * @return 值
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    /**
     * 获取属性
     *
     * @param name         属性名
     * @param defaultValue 默认值
     * @return 值
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getAttribute(String name, T defaultValue) {
        return (T)attributes.getOrDefault(name, defaultValue);
    }

    /**
     * 设置属性
     *
     * @param name  属性名
     * @param value 值
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
}

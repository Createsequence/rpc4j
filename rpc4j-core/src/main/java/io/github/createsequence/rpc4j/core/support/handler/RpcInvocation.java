package io.github.createsequence.rpc4j.core.support.handler;

import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * 处理器上下文
 *
 * @author huangchengxing
 */
public interface RpcInvocation {

    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    List<RemoteAddress> getRemoteAddresses();

    /**
     * 获取目标类型全限定名
     *
     * @return 目标类型全限定名
     */
    String getTargetName();

    /**
     * 获取方法名
     *
     * @return 方法名
     */
    String getMethodName();

    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    Class<?>[] getParameterTypes();

    /**
     * 获取调用参数
     *
     * @return 参数
     */
    Object[] getArguments();

    /**
     * 获取属性
     *
     * @param name 属性名
     * @return 值
     */
    <T> T getAttribute(String name);

    /**
     * 获取属性
     *
     * @param name 属性名
     * @param defaultValue 默认值
     * @return 值
     */
    <T> T getAttribute(String name, T defaultValue);

    /**
     * 设置属性
     *
     * @param name 属性名
     * @param value 值
     */
    void setAttribute(String name, Object value);
}

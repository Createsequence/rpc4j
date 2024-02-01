package io.github.createsequence.rpc4j.core.support;

import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocolBean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * 默认请求
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class DefaultRequest extends Rpc4jProtocolBean implements Request {

    /**
     * 请求ID
     */
    private final String requestId;

    /**
     * 目标类型全限定名
     */
    private final String targetName;

    /**
     * 方法名
     */
    private final String methodName;

    /**
     * 参数类型
     */
    private final Class<?>[] parameterTypes;

    /**
     * 参数
     */
    private final Object[] parameters;

    /**
     * 基于一次方法调用创建请求对象
     *
     * @param requestId 请求ID
     * @param method 方法
     * @param arguments 参数
     * @return 请求对象
     */
    public static DefaultRequest fromMethodInvocation(String requestId, Method method, Object[] arguments) {
        return new DefaultRequest(
            requestId,
            method.getDeclaringClass().getName(),
            method.getName(),
            method.getParameterTypes(),
            arguments
        );
    }
}

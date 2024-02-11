package io.github.createsequence.rpc4j.core.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 请求对象
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class Request implements Serializable {

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
    private final Object[] arguments;
}

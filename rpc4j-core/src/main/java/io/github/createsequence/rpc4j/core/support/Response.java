package io.github.createsequence.rpc4j.core.support;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * RPC调用响应对象
 *
 * @author huangchengxing
 */
public interface Response {

    /**
     * 获取本次响应对应的请求ID
     *
     * @return 请求的ID
     */
    String getRequestId();

    /**
     * 获取调用结果
     *
     * @return 调用结果
     */
    Object getResult();

    /**
     * 调用是否成功
     *
     * @return 是否
     */
    default boolean isSuccess() {
        return Objects.isNull(getThrowable());
    }

    /**
     * 获取调用异常
     *
     * @return 异常
     */
    @Nullable
    Throwable getThrowable();

    /**
     * 设置调用异常
     *
     * @param throwable 异常
     */
    void setThrowable(Throwable throwable);
}

package io.github.createsequence.rpc4j.core.support;

import io.github.createsequence.common.Ordered;
import io.github.createsequence.common.Rpc4jException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * 请求拦截器，该组件同时适用于服务端和客户端，
 * 允许用户在请求发送处理（客户端）或处理请求（服务端）前后进行一些增强处理
 *
 * @author huangchengxing
 * @see RequestInterceptorSupport
 */
public interface RequestInterceptor extends Ordered {

    /**
     * 在请求之前执行
     *
     * @param request 请求
     * @return 请求, 如果返回{@code null}, 则不会继续执行请求
     */
    @Nullable
    default Request beforeRequest(Request request) {
        return request;
    }

    /**
     * 在请求之后执行
     *
     * @param request  请求
     * @param response 响应
     * @return 响应
     */
    @NonNull
    default Response afterResponse(Request request, Response response) {
        return response;
    }

    /**
     * 在所有请求之后执行
     *
     * @param request  请求
     * @param response 响应
     * @param ex       请求过程中抛出的异常
     * @return 响应
     */
    @NonNull
    default Response afterCompletion(Request request, Response response, Throwable ex) {
        if (Objects.nonNull(ex)) {
            throw new Rpc4jException(ex);
        }
        return response;
    }
}

package io.github.createsequence.rpc4j.core.transport.netty;

import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.rpc4j.core.compress.CompressionType;
import io.github.createsequence.rpc4j.core.serialize.SerializationType;
import io.github.createsequence.rpc4j.core.support.DefaultResponse;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.RequestInterceptor;
import io.github.createsequence.rpc4j.core.support.Response;
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocolBean;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * 用于为请求添加Rpc4j协议支持的拦截器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class Rpc4jProtocolSupportInterceptor implements RequestInterceptor {

    private final ProtocolVersion protocolVersion;
    private final SerializationType serializationType;
    private final CompressionType compressionType;

    /**
     * 在请求之前执行
     *
     * @param request 请求
     * @return 请求, 如果返回{@code null}, 则不会继续执行请求
     */
    @Nullable
    @Override
    public Request beforeRequest(Request request) {
        if (request instanceof Rpc4jProtocolBean req) {
            req.setProtocolVersion(protocolVersion.getCode());
            req.setSerializationType(serializationType.getCode());
            req.setCompressionType(compressionType.getCode());
        }
        return request;
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
    @Override
    public Response afterCompletion(Request request, Response response, Throwable ex) {
        response = Objects.nonNull(response) ?
            response : new DefaultResponse(request.getRequestId(), null, new Rpc4jException("请求失败！"));
        if (response instanceof Rpc4jProtocolBean rsp) {
            rsp.setProtocolVersion(protocolVersion.getCode());
            rsp.setSerializationType(serializationType.getCode());
            rsp.setCompressionType(compressionType.getCode());
        }
        return response;
    }
}

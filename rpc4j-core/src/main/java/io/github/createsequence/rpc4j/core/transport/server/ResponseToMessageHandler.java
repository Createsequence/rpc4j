package io.github.createsequence.rpc4j.core.transport.server;

import io.github.createsequence.rpc4j.core.support.handler.Depends;
import io.github.createsequence.rpc4j.core.support.handler.RequiredAttributeCheckHandlerDelegate;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Attributes;
import io.github.createsequence.rpc4j.core.transport.Message;
import io.github.createsequence.rpc4j.core.transport.Response;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocol;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Netty请求响应处理器，用于将方法的调用结果转为{@link Message}
 *
 * @author huangchengxing
 */
@Depends({
    @Depends.Attr(name = Attributes.REQUEST_PROTOCOL_VERSION, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.COMPRESSION_TYPE, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.SERIALIZATION_TYPE, type = Byte.class, required = true)
})
public class ResponseToMessageHandler extends RequiredAttributeCheckHandlerDelegate {

    public ResponseToMessageHandler(RpcInvocationHandler delegate) {
        super(delegate);
    }

    /**
     * 获取依赖注解
     *
     * @return 依赖注解
     */
    @Nullable
    @Override
    protected Depends retrieveDependsAnnotation() {
        return this.getClass().getDeclaredAnnotation(Depends.class);
    }

    /**
     * 调用完成后处理
     *
     * @param rpcInvocation     调用参数
     * @param delegateResult 委托结果
     * @param throwable      异常
     * @return 返回值
     */
    @Override
    protected Object afterCompletion(
        RpcInvocation rpcInvocation, Object delegateResult, @Nullable Throwable throwable) throws Throwable {
        String requestId = rpcInvocation.getAttribute(Attributes.REQUEST_ID);
        Response response = Objects.isNull(throwable) ?
            Response.success(requestId, delegateResult) : Response.fail(requestId, throwable.getMessage());
        // 构建响应消息
        byte protocolVersion = rpcInvocation.getAttribute(Attributes.REQUEST_PROTOCOL_VERSION);
        byte compressionType = rpcInvocation.getAttribute(Attributes.COMPRESSION_TYPE);
        byte serializationType = rpcInvocation.getAttribute(Attributes.SERIALIZATION_TYPE);
        byte messageType = Rpc4jProtocol.MessageType.RPC_RESPONSE.getCode();
        return new Message<>(
            protocolVersion, messageType, serializationType, compressionType, response
        );
    }
}

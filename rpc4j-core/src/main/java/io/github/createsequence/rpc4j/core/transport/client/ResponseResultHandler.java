package io.github.createsequence.rpc4j.core.transport.client;

import io.github.createsequence.common.exception.RequestFailException;
import io.github.createsequence.rpc4j.core.support.handler.InvocationHandlerDelegate;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Response;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * 响应结果处理器，用于将{@link Response}转为结果
 *
 * @author huangchengxing
 */
public class ResponseResultHandler extends InvocationHandlerDelegate {

    public ResponseResultHandler(RpcInvocationHandler delegate) {
        super(delegate);
    }

    /**
     * 调用完成后处理
     *
     * @param rpcInvocation  调用参数
     * @param delegateResult 委托结果
     * @param throwable      异常
     * @return 返回值
     */
    @Override
    protected Object afterCompletion(
        RpcInvocation rpcInvocation, Object delegateResult, @Nullable Throwable throwable) throws Throwable {
        if (Objects.nonNull(throwable)) {
            throw throwable;
        }
        if (delegateResult instanceof Response resp) {
            if (resp.isSuccess()) {
                return resp.getResult();
            }
            throw new RequestFailException(resp.getMessage());
        }
        return delegateResult;
    }
}

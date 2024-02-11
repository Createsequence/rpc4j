package io.github.createsequence.rpc4j.core.support.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * 用于代理调用的处理器模板类
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public abstract class InvocationHandlerDelegate implements RpcInvocationHandler {

    /**
     * 委托对象
     */
    @NonNull
    @Getter
    private final RpcInvocationHandler delegate;

    /**
     * <p>获取排序值。<br />
     * 值越小，对象的优先级越高。
     *
     * @return 排序值
     */
    @Override
    public int getOrder() {
        return delegate.getOrder();
    }

    /**
     * 执行方法调用
     *
     * @param rpcInvocation 调用参数
     * @return 返回值
     * @throws Throwable 异常
     */
    @Override
    public final Object invoke(RpcInvocation rpcInvocation) throws Throwable {
        Object result = null;
        Throwable throwable = null;
        try {
            RpcInvocation beforeInvoke = beforeInvoke(rpcInvocation);
            result = delegate.invoke(beforeInvoke);
            result = afterInvoke(beforeInvoke, result);
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            result = afterCompletion(rpcInvocation, result, throwable);
        }
        return result;
    }

    /**
     * 调用前处理
     *
     * @param rpcInvocation 调用参数
     * @return 调用参数
     */
    protected RpcInvocation beforeInvoke(RpcInvocation rpcInvocation) {
        return rpcInvocation;
    }

    /**
     * 调用后处理
     *
     * @param rpcInvocation 调用参数
     * @param delegateResult 委托结果
     * @return 返回值
     * @throws Throwable 异常
     */
    protected Object afterInvoke(RpcInvocation rpcInvocation, Object delegateResult) throws Throwable {
        return delegateResult;
    }

    /**
     * 调用完成后处理
     *
     * @param rpcInvocation 调用参数
     * @param delegateResult 委托结果
     * @param throwable 异常
     * @return 返回值
     */
    protected Object afterCompletion(
        RpcInvocation rpcInvocation, Object delegateResult, @Nullable Throwable throwable) throws Throwable {
        if (Objects.nonNull(throwable)) {
            throw throwable;
        }
        return delegateResult;
    }
}

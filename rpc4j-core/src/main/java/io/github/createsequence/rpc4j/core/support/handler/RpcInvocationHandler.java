package io.github.createsequence.rpc4j.core.support.handler;

import io.github.createsequence.common.Ordered;

/**
 * 调用处理器，客户端通过该接口向服务端发起方法调用，而服务端通过该接口执行方法调用
 *
 * @author huangchengxing
 */
public interface RpcInvocationHandler extends Ordered {

    /**
     * 执行方法调用
     *
     * @param rpcInvocation 调用参数
     * @return 返回值
     * @throws Throwable 异常
     */
    Object invoke(RpcInvocation rpcInvocation) throws Throwable;
}

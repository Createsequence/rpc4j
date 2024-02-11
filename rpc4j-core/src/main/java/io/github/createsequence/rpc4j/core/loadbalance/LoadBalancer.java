package io.github.createsequence.rpc4j.core.loadbalance;

import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * 负载均衡器
 *
 * @author huangchengxing
 */
public interface LoadBalancer {

    /**
     * 选择一个地址
     *
     * @param addresses 地址列表
     * @param rpcInvocation 请求
     * @return 地址
     */
    RemoteAddress select(List<RemoteAddress> addresses, RpcInvocation rpcInvocation);
}

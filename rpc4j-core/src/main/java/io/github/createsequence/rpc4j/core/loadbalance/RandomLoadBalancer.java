package io.github.createsequence.rpc4j.core.loadbalance;

import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡策略
 *
 * @author huangchengxing
 */
public class RandomLoadBalancer implements LoadBalancer {

    private static final Random RANDOM = new Random();

    /**
     * 选择一个地址
     *
     * @param addresses 地址列表
     * @param rpcInvocation 请求
     * @return 地址
     */
    @Override
    public RemoteAddress select(List<RemoteAddress> addresses, RpcInvocation rpcInvocation) {
        return addresses.get(RANDOM.nextInt(addresses.size()));
    }
}

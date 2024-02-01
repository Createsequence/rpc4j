package io.github.createsequence.rpc4j.core.client.loadbalance;

import io.github.createsequence.rpc4j.core.support.Request;

import java.net.InetSocketAddress;
import java.util.Collection;
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
     * @param request   请求
     * @return 地址
     */
    @Override
    public InetSocketAddress select(List<InetSocketAddress> addresses, Request request) {
        return addresses.get(RANDOM.nextInt(addresses.size()));
    }
}

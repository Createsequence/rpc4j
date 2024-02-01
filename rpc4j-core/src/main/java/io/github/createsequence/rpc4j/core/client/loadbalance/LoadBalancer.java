package io.github.createsequence.rpc4j.core.client.loadbalance;

import io.github.createsequence.rpc4j.core.support.Request;

import java.net.InetSocketAddress;
import java.util.Collection;
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
     * @param request  请求
     * @return 地址
     */
    InetSocketAddress select(List<InetSocketAddress> addresses, Request request);
}

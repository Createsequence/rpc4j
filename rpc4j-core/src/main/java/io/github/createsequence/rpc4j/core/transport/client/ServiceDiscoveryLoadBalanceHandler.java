package io.github.createsequence.rpc4j.core.transport.client;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.rpc4j.core.discoverer.ServiceDiscoverer;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * 服务发现调用器，用于在客户端调用前确认服务端地址
 *
 * @author huangchengxing
 */
public class ServiceDiscoveryLoadBalanceHandler extends LoadBalanceHandler {

    private final ServiceDiscoverer serviceDiscoverer;

    public ServiceDiscoveryLoadBalanceHandler(
        RpcInvocationHandler delegate, ComponentManager componentManager,
        ServiceDiscoverer serviceDiscoverer) {
        super(delegate, componentManager);
        this.serviceDiscoverer = serviceDiscoverer;
    }

    /**
     * 获取远程地址
     *
     * @param rpcInvocation 调用参数
     * @return 远程地址
     */
    @Override
    protected List<RemoteAddress> getRemoteAddresses(RpcInvocation rpcInvocation) {
        return rpcInvocation.getRemoteAddresses().stream()
            .filter(address -> address.getType() == RemoteAddress.Type.SERVICE_NAME)
            .map(address -> serviceDiscoverer.getServices(address.getHost()))
            .flatMap(List::stream)
            .toList();
    }
}

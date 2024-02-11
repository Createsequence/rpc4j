package io.github.createsequence.rpc4j.core.transport.client;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.rpc4j.core.loadbalance.LoadBalancer;
import io.github.createsequence.rpc4j.core.support.handler.Depends;
import io.github.createsequence.rpc4j.core.support.handler.InvocationHandlerDelegate;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Attributes;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * 负载均衡调用器，用于在客户端调用前确认服务端地址
 *
 * @author huangchengxing
 */
@Depends(
    @Depends.Attr(name = Attributes.LOAD_BALANCE_STRATEGY, type = String.class)
)
public class LoadBalanceHandler extends InvocationHandlerDelegate {

    private final ComponentManager componentManager;

    public LoadBalanceHandler(
        RpcInvocationHandler delegate, ComponentManager componentManager) {
        super(delegate);
        this.componentManager = componentManager;
    }

    /**
     * 调用前处理
     *
     * @param rpcInvocation 调用参数
     * @return 调用参数
     */
    @Override
    protected RpcInvocation beforeInvoke(RpcInvocation rpcInvocation) {
        List<RemoteAddress> addresses = rpcInvocation.getRemoteAddresses();
        LoadBalancer loadBalancer = componentManager.getComponent(LoadBalancer.class, rpcInvocation.getAttribute(Attributes.LOAD_BALANCE_STRATEGY));
        RemoteAddress address = loadBalancer.select(addresses, rpcInvocation);
        rpcInvocation.setAttribute(Attributes.REMOTE_ADDRESS, address);
        return rpcInvocation;
    }

    /**
     * 获取远程地址
     *
     * @param rpcInvocation 调用参数
     * @return 远程地址
     */
    protected List<RemoteAddress> getRemoteAddresses(RpcInvocation rpcInvocation) {
        return rpcInvocation.getRemoteAddresses();
    }
}

package io.github.createsequence.rpc4j.core.support.service;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.rpc4j.core.discoverer.ServiceDiscoverer;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocol;
import io.github.createsequence.rpc4j.core.transport.client.LoadBalanceHandler;
import io.github.createsequence.rpc4j.core.transport.client.NettyClientRequestHandler;
import io.github.createsequence.rpc4j.core.transport.client.ResponseResultHandler;
import io.github.createsequence.rpc4j.core.transport.client.ServiceDiscoveryLoadBalanceHandler;
import io.github.createsequence.rpc4j.core.transport.server.NettyServer;
import io.github.createsequence.rpc4j.core.transport.server.ReflectiveMethodInvokeHandler;
import io.github.createsequence.rpc4j.core.transport.server.ResponseToMessageHandler;
import io.github.createsequence.rpc4j.core.transport.server.Server;
import lombok.experimental.Delegate;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * RPC服务管理器，用于服务的注册和引用。
 * 所有与其他的客户端/服务端的通信都使用Netty完成，
 * 通信协议默认为{@link Rpc4jProtocol rpc4j协议}。
 *
 * @author huangchengxing
 */
public class Rpc4jNettyServiceManager implements ServiceRegistry, ServiceProvider, Server {

    @Delegate(types = ServiceProvider.class)
    private final ServiceProvider serviceProvider;
    private final String serverHost;
    private final int serverPort;
    private final ServiceDiscoverer serviceDiscoverer;
    @Delegate
    private final Server server;
    private final ReflectiveMethodInvokeHandler methodInvokeHandler;

    /**
     * 创建一个实例
     *
     * @param componentManager  组件管理器
     * @param serviceDiscoverer 服务发现器
     */
    public Rpc4jNettyServiceManager(
        ComponentManager componentManager, @Nullable ServiceDiscoverer serviceDiscoverer,
        String serverHost, int serverPort) {

        // 服务端组件
        this.serviceDiscoverer = serviceDiscoverer;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.methodInvokeHandler = new ReflectiveMethodInvokeHandler();
        this.server = new NettyServer(
            componentManager, new ResponseToMessageHandler(this.methodInvokeHandler)
        );

        // 客户端组件
        RpcInvocationHandler serverInvocationHandler = new NettyClientRequestHandler(componentManager);
        serverInvocationHandler = Objects.nonNull(serviceDiscoverer) ?
            new ServiceDiscoveryLoadBalanceHandler(serverInvocationHandler, componentManager, serviceDiscoverer) :
            new LoadBalanceHandler(serverInvocationHandler, componentManager);
        serverInvocationHandler = new ResponseResultHandler(serverInvocationHandler);
        this.serviceProvider = new ReferenceServiceProvider(serverInvocationHandler);
    }

    /**
     * 向客户端暴露一个服务
     *
     * @param interfaceClass 接口类
     * @param service 服务实例
     */
    @Override
    public void export(Class<?> interfaceClass, Object service) {
        this.methodInvokeHandler.registerService(interfaceClass, service);
        if (Objects.nonNull(serviceDiscoverer)) {
            RemoteAddress remoteAddress = new RemoteAddress(RemoteAddress.Type.FIXED_IP, serverHost, serverPort);
            serviceDiscoverer.registerService(getServerKey(interfaceClass), remoteAddress);
        }
    }

    /**
     * 取消暴露一个服务
     *
     * @param interfaceClass 接口类
     */
    @Override
    public void unexport(Class<?> interfaceClass) {
        this.methodInvokeHandler.unregisterService(interfaceClass);
        if (Objects.nonNull(serviceDiscoverer)) {
            RemoteAddress remoteAddress = new RemoteAddress(RemoteAddress.Type.FIXED_IP, serverHost, serverPort);
            serviceDiscoverer.unregisterService(getServerKey(interfaceClass), remoteAddress);
        }
    }

    private String getServerKey(Class<?> interfaceClass) {
        return interfaceClass.getName();
    }
}

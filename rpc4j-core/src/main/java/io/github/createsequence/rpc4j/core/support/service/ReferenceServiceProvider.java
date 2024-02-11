package io.github.createsequence.rpc4j.core.support.service;

import io.github.createsequence.common.exception.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.support.handler.DefaultInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Attributes;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocol;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 基于{@link Reference}注解的服务提供者
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReferenceServiceProvider implements ServiceProvider {

    /**
     * Rpc调用处理器
     */
    private final RpcInvocationHandler invocationHandler;

    /**
     * 从服务端引用一个服务
     *
     * @param interfaceClass 接口类
     * @return 服务对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T refer(Class<T> interfaceClass) {
        Reference reference = interfaceClass.getAnnotation(Reference.class);
        Asserts.isNotNull(reference, "目标接口必须使用@Reference注解：{}", interfaceClass.getName());
        return (T)Proxy.newProxyInstance(
            interfaceClass.getClassLoader(), new Class[]{ interfaceClass, Referenced.class },
            new ReferenceInvocationHandler(reference)
        );
    }

    /**
     * 创建一个包含基本调用参数的RPC上下文
     *
     * @param annotation 注解
     * @param method 方法
     * @param arguments 参数
     * @return 调用上下文
     */
    protected RpcInvocation createRpcInvocation(
        Reference annotation, Method method, Object[] arguments) {
        Asserts.isNotNull(annotation.address(), "服务地址不能为空");
        List<RemoteAddress> remoteAddresses = Stream.of(annotation.address())
            .map(address -> new RemoteAddress(address.type(), address.host(), address.port()))
            .toList();
        return new DefaultInvocation(
            remoteAddresses,
            method.getDeclaringClass().getName(), method.getName(),
            method.getParameterTypes(), arguments
        );
    }

    /**
     * 准备调用参数
     *
     * @param annotation 注解
     * @param invocation 调用上下文
     * @see Attributes
     */
    protected void prepareInvocation(Reference annotation, RpcInvocation invocation) {
        invocation.setAttribute(Attributes.REQUEST_ID, UUID.randomUUID().toString());
        // 超时时间
        invocation.setAttribute(Attributes.REQUEST_TIMEOUT, annotation.timeout());
        invocation.setAttribute(Attributes.REQUEST_TIMEOUT_UNIT, annotation.timeUnit());
        // 协议版本
        invocation.setAttribute(
            Attributes.REQUEST_PROTOCOL_VERSION, Rpc4jProtocol.Version.valueOf(annotation.version()).getCode()
        );
        // 序列化方式
        invocation.setAttribute(
            Attributes.SERIALIZATION_TYPE, Rpc4jProtocol.SerializationType.valueOf(annotation.serializer()).getCode()
        );
        // 压缩方式
        invocation.setAttribute(
            Attributes.COMPRESSION_TYPE, Rpc4jProtocol.CompressionType.valueOf(annotation.compressor()).getCode()
        );
        // 负载均衡策略
        invocation.setAttribute(Attributes.LOAD_BALANCE_STRATEGY, annotation.loadBalancer());
    }

    @RequiredArgsConstructor
    protected class ReferenceInvocationHandler implements InvocationHandler {
        private final Reference annotation;
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> this.toString();
                case "hashCode" -> this.hashCode();
                case "equals" -> this.equals(args[0]);
                default -> doInvoke(method, args);
            };
        }

        private Object doInvoke(Method method, Object[] args) {
            RpcInvocation invocation = createRpcInvocation(annotation, method, args);
            prepareInvocation(annotation, invocation);
            try {
                return invocationHandler.invoke(invocation);
            } catch (Throwable e) {
                throw new Rpc4jException(e);
            }
        }
    }

    /**
     * 用于标记被引用的服务
     */
    public interface Referenced { }
}

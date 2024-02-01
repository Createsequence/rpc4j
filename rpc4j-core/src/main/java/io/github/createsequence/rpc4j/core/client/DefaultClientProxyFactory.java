package io.github.createsequence.rpc4j.core.client;

import io.github.createsequence.rpc4j.core.support.DefaultRequest;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;

/**
 * 默认的代理工厂，基于JDK代理实现
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class DefaultClientProxyFactory implements ClientProxyFactory {

    private final Client client;

    /**
     * 获取代理对象
     *
     * @param interfaceClass 接口类
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxy(Class<T> interfaceClass) {
        return (T)Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class[]{interfaceClass, ServiceProviderProxy.class},
            new ServiceProviderInvocationHandler()
        );
    }

    protected Object sendRequest(Method method, Object[] args) {
        Request request = DefaultRequest.fromMethodInvocation(UUID.randomUUID().toString(), method, args);
        Response response = client.request(request);
        return response.getResult();
    }

    /**
     * 调用处理器
     */
    public class ServiceProviderInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "equals" -> Objects.equals(this, args[0]);
                case "hashCode" -> this.hashCode();
                case "toString" -> this.toString();
                default -> sendRequest(method, args);
            };
        }
    }

    /**
     * 标识性接口，用于判断对象是否是代理对象
     */
    public interface ServiceProviderProxy { }
}

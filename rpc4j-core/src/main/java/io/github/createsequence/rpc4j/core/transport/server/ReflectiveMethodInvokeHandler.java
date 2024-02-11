package io.github.createsequence.rpc4j.core.transport.server;

import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.common.util.ClassUtils;
import io.github.createsequence.common.util.ReflectUtils;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端请求处理器，用于将请求转发到服务端的指定服务
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class ReflectiveMethodInvokeHandler implements RpcInvocationHandler {

    private static final Object MISSING_SERVICE = new Object();
    private final Map<Class<?>, Object> registeredServices = new ConcurrentHashMap<>();

    /**
     * 发送请求
     *
     * @param rpcInvocation 调用
     * @return 响应
     */
    @Override
    public Object invoke(RpcInvocation rpcInvocation) {
        // 获取要调用的方法
        Class<?> serviceType = ClassUtils.forName(rpcInvocation.getTargetName());
        Method method = ReflectUtils.getMethod(
            serviceType, rpcInvocation.getMethodName(), rpcInvocation.getParameterTypes()
        );
        Asserts.isNotNull(method, "要调用的方法不存在：{}", rpcInvocation.getMethodName());

        // 获取要调用的对象
        Object service = getService(serviceType);
        Asserts.isNotNull(service, "要调用的服务不存在：{}", rpcInvocation.getTargetName());
        // 调用方法
        return ReflectUtils.invoke(service, method, rpcInvocation.getArguments());
    }

    /**
     * 注册服务
     *
     * @param serviceType 服务类型
     * @param service 服务实例
     */
    public void registerService(Class<?> serviceType, Object service) {
        registeredServices.put(serviceType, service);
    }

    /**
     * 注销服务
     *
     * @param serviceType 服务类型
     */
    public void unregisterService(Class<?> serviceType) {
        registeredServices.put(serviceType, MISSING_SERVICE);
    }

    /**
     * 获取服务实例
     *
     * @param serviceType 目标类型
     * @return 服务实例
     */
    protected Object getService(Class<?> serviceType) {
        Object service = registeredServices.computeIfAbsent(
            serviceType, this::newServiceInstance
        );
        Asserts.isFalse(service == MISSING_SERVICE, "服务不存在：{}", serviceType.getName());
        return service;
    }

    /**
     * 通过反射创建服务实例
     *
     * @param serviceType 服务类型
     * @return 服务实例
     */
    protected Object newServiceInstance(Class<?> serviceType) {
        try {
            return serviceType.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            log.error("创建服务实例失败", ex);
        }
        return MISSING_SERVICE;
    }
}

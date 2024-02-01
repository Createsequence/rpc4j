package io.github.createsequence.rpc4j.core.server;

import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.rpc4j.core.server.ServerProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangchengxing
 */
public class DefaultServerProvider implements ServerProvider {

    private final Map<Class<?>, Object> registeredServices = new ConcurrentHashMap<>();

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
     * 获取服务实例
     *
     * @param serviceType 目标类型
     * @return 服务实例
     */
    @Override
    public Object getService(Class<?> serviceType) {
        return registeredServices.computeIfAbsent(
            serviceType, t -> {
                try {
                    return t.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new Rpc4jException(ex);
                }
            }
        );
    }
}

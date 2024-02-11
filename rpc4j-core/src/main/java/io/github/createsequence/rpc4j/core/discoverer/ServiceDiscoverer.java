package io.github.createsequence.rpc4j.core.discoverer;

import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * 服务发现者
 *
 * @author huangchengxing
 */
public interface ServiceDiscoverer {

    /**
     * 根据服务名称获取服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    List<RemoteAddress> getServices(String serviceName);

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param address    服务地址
     */
    void registerService(String serviceName, RemoteAddress address);

    /**
     * 移除服务
     *
     * @param serviceName 服务名称
     * @param address   服务地址
     */
    void unregisterService(String serviceName, RemoteAddress address);
}

package io.github.createsequence.rpc4j.core.discoverer;

import io.github.createsequence.common.util.MultiMap;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.util.List;

/**
 * @author huangchengxing
 */
public class LocalServiceDiscoverer implements ServiceDiscoverer {

    private final MultiMap<String, RemoteAddress> registeredServices = MultiMap.arrayListMultimap();

    /**
     * 根据服务名称获取服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    @Override
    public List<RemoteAddress> getServices(String serviceName) {
        return (List<RemoteAddress>)registeredServices.get(serviceName);
    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    @Override
    public void registerService(String serviceName, RemoteAddress address) {
        registeredServices.put(serviceName, address);
    }

    /**
     * 移除服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    @Override
    public void unregisterService(String serviceName, RemoteAddress address) {
        List<RemoteAddress> addresses = getServices(serviceName);
        if (!addresses.isEmpty()) {
            addresses.remove(address);
        }
    }
}

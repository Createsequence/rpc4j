package io.github.createsequence.rpc4j.core.discoverer;

import io.github.createsequence.common.util.MultiMap;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author huangchengxing
 */
public class LocalServiceDiscoverer implements ServiceDiscoverer {

    private MultiMap<String, InetSocketAddress> registeredServices = MultiMap.arrayListMultimap();

    /**
     * 根据服务名称获取服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    @Override
    public List<InetSocketAddress> getServices(String serviceName) {
        return (List<InetSocketAddress>)registeredServices.get(serviceName);
    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    @Override
    public void registerService(String serviceName, InetSocketAddress address) {
        registeredServices.put(serviceName, address);
    }

    /**
     * 移除服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    @Override
    public void removeService(String serviceName, InetSocketAddress address) {
        List<InetSocketAddress> addresses = getServices(serviceName);
        if (!addresses.isEmpty()) {
            addresses.remove(address);
        }
    }
}

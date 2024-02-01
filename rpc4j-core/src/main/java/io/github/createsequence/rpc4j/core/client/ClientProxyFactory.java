package io.github.createsequence.rpc4j.core.client;

/**
 * 代理对象工厂
 *
 * @author huangchengxing
 */
public interface ClientProxyFactory {

    /**
     * 获取代理对象
     *
     * @param interfaceClass 接口类
     * @param <T> 接口类型
     * @return 代理对象
     */
    <T> T getProxy(Class<T> interfaceClass);
}

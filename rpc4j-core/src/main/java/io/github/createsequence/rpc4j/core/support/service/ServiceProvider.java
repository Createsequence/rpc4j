package io.github.createsequence.rpc4j.core.support.service;

/**
 * 服务提供者，客户端通过它来引用服务
 *
 * @author huangchengxing
 */
public interface ServiceProvider {

    /**
     * 从服务端引用一个服务
     *
     * @param interfaceClass 接口类
     * @return 服务对象
     */
    <T> T refer(Class<T> interfaceClass);
}

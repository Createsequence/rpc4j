package io.github.createsequence.rpc4j.core.support.service;

import io.github.createsequence.rpc4j.core.transport.server.Server;

/**
 * 服务注册表，服务端通过它来暴露服务
 *
 * @author huangchengxing
 */
public interface ServiceRegistry extends Server {

    /**
     * 向客户端暴露一个服务
     *
     * @param interfaceClass 接口类
     * @param service 服务实例
     */
    void export(Class<?> interfaceClass, Object service);

    /**
     * 取消暴露一个服务
     *
     * @param interfaceClass 接口类
     */
    void unexport(Class<?> interfaceClass);
}

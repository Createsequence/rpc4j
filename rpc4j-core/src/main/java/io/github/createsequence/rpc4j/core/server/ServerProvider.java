package io.github.createsequence.rpc4j.core.server;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 服务提供者
 *
 * @author huangchengxing
 */
public interface ServerProvider {

    /**
     * 获取服务实例
     *
     * @param serviceType 服务类型
     * @return 服务实例
     */
    Object getService(Class<?> serviceType);
}

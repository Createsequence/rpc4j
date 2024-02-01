package io.github.createsequence.rpc4j.core.client;

import java.io.Closeable;
import java.net.InetSocketAddress;

/**
 * 连接工厂
 *
 * @author huangchengxing
 */
public interface ConnectionFactory extends Closeable {

    /**
     * 获取连接
     *
     * @param address 地址
     * @return 连接
     */
    Connection getConnection(InetSocketAddress address);
}

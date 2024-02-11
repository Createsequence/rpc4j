package io.github.createsequence.rpc4j.core.transport.server;

/**
 * 服务端
 *
 * @author huangchengxing
 */
public interface Server {

    /**
     * 启动服务
     *
     * @param host 主机地址
     * @param port 端口号
     */
    void start(String host, int port);

    /**
     * 获取服务端口
     *
     * @return 服务端口
     */
    int getPort();

    /**
     * 服务是否已经启动
     *
     * @return 是否已经启动
     */
    boolean isStarted();

    /**
     * 停止服务
     */
    void stop();

    /**
     * 服务是否已经停止
     *
     * @return 是否已经停止
     */
    boolean isStopped();
}

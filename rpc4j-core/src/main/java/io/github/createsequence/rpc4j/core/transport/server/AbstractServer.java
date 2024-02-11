package io.github.createsequence.rpc4j.core.transport.server;

import io.github.createsequence.common.exception.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Server}的基础实现
 *
 * @author huangchengxing
 */
@Slf4j
@Getter
public abstract class AbstractServer implements Server {

    /**
     * 端口号
     */
    private int port;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 是否已经启动
     */
    private boolean started;

    /**
     * 启动服务
     *
     * @param host 主机地址
     * @param port 端口号
     */
    @Override
    public void start(String host, int port) {
        Asserts.isTrue(!started, "服务已经启动");
        this.host = host;
        this.port = port;
        try {
            doStart(host, port);
            started = true;
            log.info("服务启动完成，主机地址[{}]，监听端口[{}]", host, port);
        } catch (Throwable e) {
            log.error("启动服务失败", e);
            throw new Rpc4jException("启动服务失败", e);
        } finally {
            if (!started) {
                doStop();
            }
        }
    }

    /**
     * 启动服务
     *
     * @param host 主机地址
     * @param port 端口号
     * @throws Throwable 异常
     */
    protected abstract void doStart(String host, int port) throws Throwable;

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        Asserts.isTrue(started, "服务未启动!");
        try {
            doStop();
            started = false;
            log.info("服务停止完成，主机地址[{}]，监听端口[{}]", host, port);
        } catch (Throwable e) {
            log.error("停止服务失败", e);
            throw new Rpc4jException("停止服务失败", e);
        }
    }

    /**
     * 停止服务
     */
    protected abstract void doStop();

    /**
     * 服务是否已经停止
     *
     * @return 是否已经停止
     */
    @Override
    public boolean isStopped() {
        return !started;
    }
}

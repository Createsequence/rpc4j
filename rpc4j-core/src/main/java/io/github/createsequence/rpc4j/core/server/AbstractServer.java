package io.github.createsequence.rpc4j.core.server;

import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * {@link Server}的基础实现
 *
 * @author huangchengxing
 */
@Slf4j
public abstract class AbstractServer implements Server {

    /**
     * 运行ID
     */
    @Getter
    private final String runId;

    /**
     * 端口号
     */
    @Getter
    private final int port;

    /**
     * 是否已经启动
     */
    @Getter
    private boolean started;

    /**
     * 停机时的回调
     */
    private final ShutdownHook shutdownHook;

    /**
     * 创建一个RPC服务器
     *
     * @param port 端口号
     */
    protected AbstractServer(int port) {
        this.port = port;
        this.runId = this.getClass().getSimpleName() + ":" + UUID.randomUUID();
        this.shutdownHook = new ShutdownHook(runId);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        log.info("start server [{}] on port: {}", runId, port);
        long begin = System.currentTimeMillis();
        try {
            doStart();
            this.started = true;
        } catch (Exception e) {
            log.error("start server error", e);
            shutdownHook.start();
            throw new Rpc4jException(e);
        } finally {
            long end = System.currentTimeMillis();
            log.warn("started server [{}] in {} ms", runId, end - begin);
        }
    }

    /**
     * 启动服务
     */
    protected abstract void doStart();

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        if (!started) {
            return;
        }
        log.info("stop server [{}] on port: {}", runId, port);
        long begin = System.currentTimeMillis();
        try {
            this.started = false;
            doStop();
        } catch (Exception e) {
            log.error("stop server error", e);
            shutdownHook.start();
            throw new Rpc4jException(e);
        } finally {
            long end = System.currentTimeMillis();
            log.warn("stopped server [{}] in {} ms", runId, end - begin);
        }
    }

    /**
     * 停止服务
     */
    public abstract void doStop();

    /**
     * 设置停机时的回调
     *
     * @param taskName 任务名
     * @param hook 回调
     */
    @Override
    public void addShutdownHook(String taskName, Consumer<String> hook) {
        shutdownHook.addShutdownHook(taskName, hook);
    }

    /**
     * 清除停机时的回调
     */
    @Override
    public List<Consumer<String>> clearAllShutdownHooks() {
        return shutdownHook.clearAllShutdownHooks();
    }

    /**
     * 停机时的回调
     */
    @RequiredArgsConstructor
    protected static class ShutdownHook extends Thread {

        @Getter
        private boolean executed;
        private final String runId;
        private final Map<String, Consumer<String>> hooks = new LinkedHashMap<>();

        @Override
        public synchronized void run() {
            check();
            log.info("call shutdown hook for server [{}]", runId);
            hooks.forEach((name, hook) -> {
                log.info("execute shutdown hook {} ......", name);
                try {
                    hook.accept(runId);
                } catch (Exception e) {
                    log.error("execute shutdown hook error", e);
                }
            });
            log.info("all shutdown hook for server [{}] done", runId);
            hooks.clear();
            this.executed = true;
        }

        public synchronized void addShutdownHook(String taskName, Consumer<String> hook) {
            check();
            hooks.put(taskName, hook);
        }

        public synchronized List<Consumer<String>> clearAllShutdownHooks() {
            check();
            List<Consumer<String>> tasks = new ArrayList<>(this.hooks.values());
            this.hooks.clear();
            return tasks;
        }

        private void check() {
            Asserts.isFalse(executed, "all hooks of server [{}] has been executed", runId);
        }
    }
}

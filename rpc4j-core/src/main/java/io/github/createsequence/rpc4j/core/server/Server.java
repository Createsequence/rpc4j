package io.github.createsequence.rpc4j.core.server;

import java.util.List;
import java.util.function.Consumer;

/**
 * 服务端
 *
 * @author huangchengxing
 */
public interface Server {

    /**
     * 默认端口号
     */
    int DEFAULT_PORT = 8080;

    /**
     * 启动服务
     */
    void start();

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
     * 添加停机时的回调，该回调在下述情况下会被调用：
     * <ul>
     *     <li>调用{@link #stop()}方法后，服务器停止运行；</li>
     *     <li>调用{@link #start()}方法后，服务器启动失败；</li>
     *     <li>JVM进程退出。</li>
     * </ul>
     * 所有的回调都会按照添加的顺序执行，并且在服务器的整个生命周期内只会执行一次。
     *
     * @param taskName 任务名
     * @param hook 回调
     */
    void addShutdownHook(String taskName, Consumer<String> hook);

    /**
     * 清除停机时的回调
     *
     * @return 还未执行的回调
     */
    List<Consumer<String>> clearAllShutdownHooks();
}

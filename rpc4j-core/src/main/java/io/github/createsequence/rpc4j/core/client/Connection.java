package io.github.createsequence.rpc4j.core.client;

import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * 抽象接口，表示当前客户端与某个服务端之间的通道
 *
 * @author huangchengxing
 */
public interface Connection extends Closeable {

    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    String getId();

    /**
     * 连接是否活跃
     *
     * @return 是否活跃
     */
    boolean isActive();

    /**
     * 建立链接，发送请求并获得响应
     *
     * @param request 请求
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 响应
     */
    Response connect(Request request, long timeout, TimeUnit timeUnit);
}

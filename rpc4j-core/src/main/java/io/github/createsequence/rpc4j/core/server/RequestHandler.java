package io.github.createsequence.rpc4j.core.server;

import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;

/**
 * 用于在服务端或者客户端处理请求的处理器
 *
 * @author huangchengxing
 */
public interface RequestHandler {

    /**
     * 处理请求
     *
     * @param request rpc请求
     * @return rpc响应
     */
    Response handle(Request request);
}

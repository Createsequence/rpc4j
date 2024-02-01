package io.github.createsequence.rpc4j.core.client;

import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;

/**
 * 用于发送请求的客户端
 *
 * @author huangchengxing
 */
public interface Client {

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 响应
     */
    Response request(Request request);
}

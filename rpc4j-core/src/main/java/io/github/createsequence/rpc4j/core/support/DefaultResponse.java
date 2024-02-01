package io.github.createsequence.rpc4j.core.support;

import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocolBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 默认响应
 *
 * @author huangchengxing
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultResponse extends Rpc4jProtocolBean implements Response {

    /**
     * 请求ID
     */
    private final String requestId;

    /**
     * 调用结果
     */
    private final Object result;

    /**
     * 调用异常
     */
    private Throwable throwable;
}

package io.github.createsequence.rpc4j.core.transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 响应对象
 *
 * @author huangchengxing
 */
@Getter
@AllArgsConstructor
public class Response implements Serializable {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";

    /**
     * 请求ID
     */
    private final String requestId;

    /**
     * 调用结果
     */
    private final Object result;

    /**
     * 响应状态
     */
    private final String status;

    /**
     * 响应消息
     */
    @Setter
    private String message;

    /**
     * 创建成功响应
     *
     * @param result 调用结果
     * @param requestId 请求ID
     * @return 响应对象
     */
    public static Response success(String requestId, Object result) {
        return new Response(requestId, result, STATUS_SUCCESS, "ok");
    }

    /**
     * 创建失败响应
     *
     * @param requestId 请求ID
     * @param message 响应消息
     * @return 响应对象
     */
    public static Response fail(String requestId, String message) {
        return new Response(requestId, null, STATUS_FAIL, message);
    }

    /**
     * 判断请求是否成功
     *
     * @return 是否
     */
    public boolean isSuccess() {
        return STATUS_SUCCESS.equals(status);
    }
}

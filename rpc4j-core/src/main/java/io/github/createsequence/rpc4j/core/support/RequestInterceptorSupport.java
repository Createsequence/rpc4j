package io.github.createsequence.rpc4j.core.support;

import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.server.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * {@link RequestHandler}的基本实现
 *
 * @author huangchengxing
 */
@Slf4j
public abstract class RequestInterceptorSupport {

    /**
     * 拦截器
     */
    private final List<RequestInterceptor> interceptors = new ArrayList<>();

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(RequestInterceptor interceptor) {
        Asserts.isNotNull(interceptor, "interceptor");
        interceptors.remove(interceptor);
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparing(RequestInterceptor::getOrder));
    }

    /**
     * 处理请求
     *
     * @param request 请求
     * @return 响应
     * @see #doHandle(Request)
     */
    protected Response handle(Request request) {
        Request newRequest = triggerBeforeRequest(request);
        Response response = null;
        Throwable exception = null;
        try {
            response = doHandle(newRequest);
            response = triggerAfterResponse(newRequest, response);
        } catch (Throwable ex) {
            ex.printStackTrace();
            exception = ex;
        } finally {
            response = triggerAfterCompletion(newRequest, response, exception);
        }
        return response;
    }

    private Response triggerAfterCompletion(Request request, Response response, Throwable ex) {
        Response newResponse = response;
        for (RequestInterceptor interceptor : interceptors) {
            newResponse = interceptor.afterCompletion(request, newResponse, ex);
        }
        return newResponse;
    }

    private Response triggerAfterResponse(Request request, Response response) {
        Response newResponse = response;
        for (RequestInterceptor interceptor : interceptors) {
            newResponse = interceptor.afterResponse(request, newResponse);
        }
        return newResponse;
    }

    private Request triggerBeforeRequest(Request request) {
        Request newRequest = request;
        for (RequestInterceptor interceptor : interceptors) {
            newRequest = interceptor.beforeRequest(newRequest);
            if (Objects.isNull(newRequest)) {
                log.warn("Request has been intercepted by {} !", interceptor);
            }
        }
        return newRequest;
    }

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 响应
     */
    protected abstract Response doHandle(Request request);
}

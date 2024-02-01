package io.github.createsequence.rpc4j.core.server;

import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.common.util.ClassUtils;
import io.github.createsequence.common.util.ReflectUtils;
import io.github.createsequence.rpc4j.core.support.RequestInterceptorSupport;
import io.github.createsequence.rpc4j.core.support.DefaultResponse;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * 服务端请求处理器，用于将请求转发到服务端的指定服务
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ServerProviderRequestHandler
    extends RequestInterceptorSupport implements RequestHandler {

    /**
     * 服务提供者
     */
    private final ServerProvider serverProvider;

    /**
     * 处理请求
     *
     * @param request rpc请求
     * @return rpc响应
     */
    @Override
    public Response handle(Request request) {
        return super.handle(request);
    }

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 响应
     */
    @Override
    protected Response doHandle(Request request) {
        String targetName = request.getTargetName();
        Throwable ex = null;
        Object result = null;
        try {
            result = invokeMethodForSpecifiedService(request, targetName);
        } catch (Exception exception) {
            exception.printStackTrace();
            ex = exception;
        }
        return new DefaultResponse(request.getRequestId(), result, ex);
    }

    private Object invokeMethodForSpecifiedService(Request request, String targetName) {
        // 获取要调用的方法
        Class<?> serviceType = ClassUtils.forName(targetName);
        Method method = ReflectUtils.getMethod(
            serviceType, request.getMethodName(), request.getParameterTypes()
        );
        Asserts.isNotNull(method, "要调用的方法不存在：{}", request.getMethodName());

        // 获取要调用的对象
        Object service = serverProvider.getService(serviceType);
        Asserts.isNotNull(service, "要调用的服务不存在：{}", targetName);
        // 调用方法
        return ReflectUtils.invoke(service, method, request.getParameters());
    }
}

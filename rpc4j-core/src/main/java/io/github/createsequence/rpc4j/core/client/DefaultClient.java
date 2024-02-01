package io.github.createsequence.rpc4j.core.client;

import io.github.createsequence.rpc4j.core.client.loadbalance.LoadBalancer;
import io.github.createsequence.rpc4j.core.discoverer.ServiceDiscoverer;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.RequestInterceptorSupport;
import io.github.createsequence.rpc4j.core.support.Response;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通用的客户端实现
 *
 * @author huangchengxing
 */
@Setter
@RequiredArgsConstructor
public class DefaultClient
    extends RequestInterceptorSupport implements Client {

    /**
     * 连接工厂
     */
    private final ConnectionFactory connectionFactory;

    /**
     * 负载均衡器
     */
    private final LoadBalancer loadBalancer;

    /**
     * 服务发现器
     */
    private final ServiceDiscoverer discoverer;

    /**
     * 请求超时时间，默认为20秒
     */
    private long timeout = 100 * 1000L;

    /**
     * 超时时间单位，默认为毫秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 响应
     */
    @Override
    public Response request(Request request) {
        return handle(request);
    }

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 响应
     */
    @Override
    protected Response doHandle(Request request) {
        InetSocketAddress address = lookupServiceAddress(request);
        Connection connection = connectionFactory.getConnection(address);
        return connection.connect(request, timeout, timeUnit);
    }

    /**
     * 获取实际请求地址
     *
     * @param request 请求
     * @return 请求地址
     */
    @NonNull
    protected InetSocketAddress lookupServiceAddress(Request request) {
        List<InetSocketAddress> addresses = discoverer.getServices(request.getTargetName());
        return loadBalancer.select(addresses, request);
    }
}

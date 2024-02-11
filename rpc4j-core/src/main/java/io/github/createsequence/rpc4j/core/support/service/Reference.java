package io.github.createsequence.rpc4j.core.support.service;

import io.github.createsequence.rpc4j.core.compress.Compressor;
import io.github.createsequence.rpc4j.core.loadbalance.LoadBalancer;
import io.github.createsequence.rpc4j.core.serialize.Serializer;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 表示一个服务引用
 *
 * @author huangchengxing
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Reference {

    // TODO 优化组件的引用方式

    /**
     * 服务端地址
     *
     * @return 地址
     */
    Address[] address();

    /**
     * 请求的超时时间
     *
     * @return 超时时间
     */
    long timeout() default 30000;

    /**
     * 超时时间单位
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 服务版本
     *
     * @return 版本
     */
    String version() default "V1";

    /**
     * 序列化方式
     *
     * @return 序列化方式
     * @see Serializer
     */
    String serializer() default "FASTJSON";

    /**
     * 负载均衡策略
     *
     * @return 负载均衡策略
     * @see LoadBalancer
     */
    String loadBalancer() default "RandomLoadBalancer";

    /**
     * 压缩方式
     *
     * @return 压缩方式
     * @see Compressor
     */
    String compressor() default "NONE";

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface Address {

        /**
         * 服务端地址类型
         *
         * @return 地址类型
         */
        RemoteAddress.Type type() default RemoteAddress.Type.FIXED_IP;

        /**
         * 服务端地址
         *
         * @return 地址
         */
        String host();

        /**
         * 服务端端口
         *
         * @return 端口
         */
        int port();
    }
}

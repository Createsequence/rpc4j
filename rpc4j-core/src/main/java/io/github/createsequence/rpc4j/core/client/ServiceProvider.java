package io.github.createsequence.rpc4j.core.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个接口，表示这个接口是一个服务接口
 *
 * @author huangchengxing
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceProvider {

    // TODO 支持设置版本号

    /**
     * 要使用的客户端代理工厂
     *
     * @return 客户端代理工厂
     */
    String clientProxyFactory() default "";

}

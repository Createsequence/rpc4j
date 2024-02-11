package io.github.createsequence.rpc4j.core.support.handler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 调用器描述
 *
 * @author huangchengxing
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Depends {

    /**
     * 依赖的属性
     *
     * @return 属性
     */
    Attr[] value() default {};

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Attr {

        /**
         * 依赖的属性名称
         *
         * @return 属性名称
         */
        String name();

        /**
         * 依赖的属性类型
         *
         * @return 属性类型
         */
        Class<?> type();

        /**
         * 是否必须
         *
         * @return 是否
         */
        boolean required() default false;
    }
}

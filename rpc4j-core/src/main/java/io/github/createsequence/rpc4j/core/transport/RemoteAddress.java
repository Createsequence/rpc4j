package io.github.createsequence.rpc4j.core.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务端地址
 *
 * @author huangchengxing
 */
@Builder
@Setter
@Getter
@AllArgsConstructor
public class RemoteAddress {

    /**
     * 地址类型
     */
    private Type type;

    /**
     * 服务端地址
     */
    private String host;

    /**
     * 服务端端口
     */
    private int port;

    /**
     * 地址类型
     *
     * @author huangchengxing 
     */
    public enum Type {
        /**
         * IP地址，适用于直接通过IP地址访问的场景
         */
        FIXED_IP,
        /**
         * 服务名称，适用于存在注册中心的场景
         */
        SERVICE_NAME
    }
}

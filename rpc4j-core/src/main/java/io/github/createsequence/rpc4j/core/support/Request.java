package io.github.createsequence.rpc4j.core.support;

/**
 * RPC调用请求对象
 *
 * @author huangchengxing
 */
public interface Request {

    /**
     * 获取本次请求的ID
     *
     * @return 请求的ID
     */
    String getRequestId();

    /**
     * 获取请求的目标类型
     *
     * @return 目标类型
     */
    String getTargetName();

    /**
     * 获取请求的方法名称
     *
     * @return 方法名称
     */
    String getMethodName();

    /**
     * 获取请求的参数类型
     *
     * @return 参数类型
     */
    Class<?>[] getParameterTypes();

    /**
     * 获取本次请求参数
     *
     * @return 请求参数
     */
    Object[] getParameters();
}

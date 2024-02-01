/**
 * <p>客户端，主要包括三个组件：
 *
 * <p><strong>连接</strong>：<br />
 * 客户端需要一个{@link io.github.createsequence.rpc4j.core.client.Connection 连接}来维护与服务器的连接，
 * 通过这个连接，客户端可以向服务的发送{@link io.github.createsequence.rpc4j.core.transport.Packet 数据包}，
 * 并得到服务端响应的{@link io.github.createsequence.rpc4j.core.transport.Packet 数据包}。<br />
 * 此外，为了避免反复创建连接，客户端通常会通过连接池来管理连接。
 *
 * <p><strong>客户端</strong>：<br />
 * 客户端需要一个{@link io.github.createsequence.rpc4j.core.client.Client 客户端}来发送请求，
 * 具体来说，当通过RPC接口进行一次本地方法调时，客户端会把它转换成一个网络{@link io.github.createsequence.rpc4j.core.support.Request 请求}，
 * 然后再{@link io.github.createsequence.rpc4j.core.client.Connection 连接}连接发送给服务端。<br />
 * 此外，客户端允许注册额外的{@link io.github.createsequence.rpc4j.core.support.RequestInterceptor 拦截器}，
 * 以便在请求发送前后进行一些增强处理。
 *
 * <p><strong>代理工厂</strong>：<br />
 * 为了便于客户端调用服务端的方法，客户端需要一个{@link io.github.createsequence.rpc4j.core.client.ClientProxyFactory 代理工厂}，
 * 该代理工厂将基于服务端的接口生成一个代理类，
 * 这个代理类将会把本地方法调用转为一个{@link io.github.createsequence.rpc4j.core.support.Request 请求}，
 * 然后通过{@link io.github.createsequence.rpc4j.core.client.Client 客户端}发送给服务端，
 * 最后再将服务端的响应转换为本地方法的返回值。
 *
 * @author huangchengxing
 */
package io.github.createsequence.rpc4j.core.client;
/**
 * <p>RPC服务端，主要包括两个组件：
 * 
 * <p><strong>服务器</strong>：<br />
 * 项目启动后，需要启动一个{@link io.github.createsequence.rpc4j.core.server.Server 服务器}监听指定端口。
 * 当接受到请求后，服务器将解析从客户端得到的{@link io.github.createsequence.rpc4j.core.transport.Packet 数据包}，
 * 并转发给{@link io.github.createsequence.rpc4j.core.server.RequestHandler 处理器}处理。
 *
 * <p><strong>处理器</strong>：<br />
 * 服务器需要一个{@link io.github.createsequence.rpc4j.core.server.RequestHandler 处理器}来处理请求，
 * 通常来说，这个处理器会再发服务器发个它的请求，进一步分发到真正执行逻辑的实现类方法去完成。<br />
 * 此外，处理器允许注册额外的{@link io.github.createsequence.rpc4j.core.support.RequestInterceptor 拦截器}，
 * 以便在请求处理前后进行一些增强处理。
 *
 * @author huangchengxing
 */
package io.github.createsequence.rpc4j.core.server;
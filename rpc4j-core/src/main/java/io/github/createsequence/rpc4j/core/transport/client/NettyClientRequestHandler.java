package io.github.createsequence.rpc4j.core.transport.client;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.exception.RequestFailException;
import io.github.createsequence.common.exception.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.support.handler.Depends;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Attributes;
import io.github.createsequence.rpc4j.core.transport.Message;
import io.github.createsequence.rpc4j.core.transport.RemoteAddress;
import io.github.createsequence.rpc4j.core.transport.Request;
import io.github.createsequence.rpc4j.core.transport.Response;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocol;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyDecoder;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Netty客户端调用器，用于执行远程方法调用
 *
 * @author huangchengxing
 */
@Depends({
    @Depends.Attr(name = Attributes.REMOTE_ADDRESS, type = RemoteAddress.class, required = true),
    @Depends.Attr(name = Attributes.REQUEST_TIMEOUT, type = Long.class),
    @Depends.Attr(name = Attributes.REQUEST_TIMEOUT_UNIT, type = TimeUnit.class),
    @Depends.Attr(name = Attributes.REQUEST_PROTOCOL_VERSION, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.COMPRESSION_TYPE, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.SERIALIZATION_TYPE, type = Byte.class, required = true)
})
@Slf4j
public class NettyClientRequestHandler implements RpcInvocationHandler, Closeable {

    private final RequestRegistry requestRegistry;
    private final Bootstrap bootstrap;
    private final EventLoopGroup worker;

    /**
     * 默认请求超时时间
     */
    @Setter
    private long defaultRequestTimeout = 5000L;

    /**
     * 默认请求超时时间单位
     */
    @Setter
    private TimeUnit defaultRequestTimeoutUnit = TimeUnit.MILLISECONDS;

    public NettyClientRequestHandler(
        ComponentManager componentManager) {
        this.requestRegistry = new RequestRegistry();
        this.worker = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
            .group(worker)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            //.handler(new LoggingHandler(LogLevel.INFO))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new Rpc4jNettyEncoder(componentManager));
                    pipeline.addLast(new Rpc4jNettyDecoder(componentManager));
                    pipeline.addLast(new ClientInboundHandler());
                }
            });
    }

    /**
     * 执行方法调用
     *
     * @param rpcInvocation 调用参数
     * @return 返回值
     */
    @Override
    public Object invoke(RpcInvocation rpcInvocation) {
        RemoteAddress remoteAddress = rpcInvocation.getAttribute(Attributes.REMOTE_ADDRESS);
        Channel channel = getChannel(new InetSocketAddress(remoteAddress.getHost(), remoteAddress.getPort()));
        Asserts.isTrue(channel.isActive(), "连接[{}]已经关闭！", channel.id());
        Asserts.isTrue(channel.isWritable(), "连接[{}]不可写！", channel.id());
        Response response = doConnect(channel, rpcInvocation);
        return resolveResult(response);
    }

    /**
     * 解析响应
     *
     * @param response 响应
     * @return 返回值
     */
    protected Object resolveResult(Response response) {
        if (response.isSuccess()) {
            return response.getResult();
        }
        throw new RequestFailException("服务端响应异常，错误信息：{}", response.getMessage());
    }

    /**
     * 获取通道
     *
     * @param address 地址
     * @return 通道
     */
    // TODO 使用连接池复用Channel
    @SneakyThrows
    protected Channel getChannel(InetSocketAddress address) {
        ChannelFuture future = bootstrap.connect(address).sync();
        if (future.isSuccess()) {
            Channel channel = future.channel();
            log.info("客户端与服务端连接成功，服务端地址[{}]，通道ID为[{}]", address.toString(), channel.id());
            return channel;
        }
        throw new Rpc4jException("无法与服务端[{}]建立连接！", address.toString());
    }

    /**
     * 建立链接，发送请求并获得响应
     *
     * @param channel 通道
     * @param rpcInvocation 调用参数
     * @return 响应
     */
    @SneakyThrows
    private Response doConnect(Channel channel, RpcInvocation rpcInvocation) {
        Asserts.isTrue(channel.isActive(), "连接[{}]已经关闭！", channel.id());

        Request request = new Request(
            UUID.randomUUID().toString(),
            rpcInvocation.getTargetName(), rpcInvocation.getMethodName(),
            rpcInvocation.getParameterTypes(), rpcInvocation.getArguments()
        );

        Long timeout = rpcInvocation.getAttribute(Attributes.REQUEST_TIMEOUT, defaultRequestTimeout);
        TimeUnit timeUnit = rpcInvocation.getAttribute(Attributes.REQUEST_TIMEOUT_UNIT, defaultRequestTimeoutUnit);
        byte protocolVersion = rpcInvocation.getAttribute(Attributes.REQUEST_PROTOCOL_VERSION);
        byte compressionType = rpcInvocation.getAttribute(Attributes.COMPRESSION_TYPE);
        byte serializationType = rpcInvocation.getAttribute(Attributes.SERIALIZATION_TYPE);
        byte messageType = Rpc4jProtocol.MessageType.RPC_REQUEST.getCode();

        // 创建并注册任务
        String requestId = request.getRequestId();
        var requestFuture = requestRegistry.register(
            request.getRequestId(), timeout, timeUnit, null
        );

        // 通过通道异步发送请求
        Message<Request> message = new Message<>(
            protocolVersion, messageType, serializationType, compressionType, request
        );
        channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端发送请求，请求ID为[{}]", requestId);
            } else {
                log.error("客户端发送请求失败，请求ID为[{}]", requestId);
                requestRegistry.complete(requestId, future.cause());
            }
        });

        // 阻塞当前线程，直到超时或任务完成
        // TODO 可选是否选用全异步模式
        return awaitUntilTimeout(requestId, requestFuture, timeout, timeUnit);
    }

    private Response awaitUntilTimeout(
        String requestId, CompletableFuture<Object> requestFuture,
        long timeout, TimeUnit timeUnit) {
        long ddl = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        Thread currentThread = Thread.currentThread();
        // 再次检查任务是否已经完成，防止由于超时时间过短导致任务完成后才开始阻塞线程
        if (!requestFuture.isDone()
            && !requestFuture.thenRun(() -> LockSupport.unpark(currentThread)).isDone()) {
            LockSupport.parkUntil(currentThread, ddl);
        }

        Object response = requestFuture.getNow(null);
        if (response instanceof Response resp) {
            return resp;
        }
        // 如果任务执行过程中发生异常
        if (response instanceof Exception ex) {
            throw new Rpc4jException(ex);
        }
        // 如果是等待超时
        throw new Rpc4jException("请求[{}]等待超时！", requestId);
    }

    @Override
    public void close() {
        worker.shutdownGracefully();
    }

    /**
     * 服务端入站请求处理器
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object target) {
            log.info("客户端接收到消息：{}", target);
            if (target instanceof Message<?> msg && msg.getPayload() instanceof Response response) {
                log.info("客户端接受响应，请求ID为[{}]", response.getRequestId());
                requestRegistry.complete(response.getRequestId(), response);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error("客户端发生异常：{}", cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }
    }

    /**
     * 请求注册表，当客户端的IO线程发起一个请求后，
     * 将会将注册表注册任务，并阻塞等待任务线程异步的完成任务。
     * 当等待超时，或者任务线程完成任务后，将会唤醒IO线程。
     *
     * @author huangchengxing
     */
    @Slf4j
    private static class RequestRegistry {

        private final ConcurrentMap<String, CompletableFuture<Object>> tasks = new ConcurrentHashMap<>();

        /**
         * 注册并返回一个任务，并且该任务：
         * <ul>
         *     <li>在发生异常或等待超时时，将会返回一个默认值；</li>
         *     <li>在任务完成后，总是将会从注册表中移除该任务；</li>
         * </ul>
         *
         * @param requestId 请求ID
         * @param timeout 超时时间
         * @param timeUnit 时间单位
         * @param defaultValue 当等待超时或执行过程中抛出异常时，需要返回的默认值
         * @return 任务执行结果
         */
        public <T> CompletableFuture<Object> register(
            String requestId, long timeout, TimeUnit timeUnit, T defaultValue) {
            Asserts.isFalse(tasks.containsKey(requestId), "已有相同ID的请求正在等待完成: {}", requestId);
            CompletableFuture<Object> future = new CompletableFuture<>()
                .completeOnTimeout(defaultValue, timeout, timeUnit)
                .exceptionally(throwable -> {
                    log.error("请求[{}]执行过程中发生异常！", requestId, throwable);
                    return defaultValue;
                });
            future.thenRun(() -> tasks.remove(requestId));
            tasks.put(requestId, future);
            return future;
        }

        /**
         * 完成请求
         *
         * @param requestId 请求ID
         * @param result 请求结果，或执行过程中发生的异常
         */
        public void complete(String requestId, Object result) {
            CompletableFuture<Object> future = tasks.get(requestId);
            if (Objects.nonNull(future) && !future.isDone()) {
                future.complete(result);
            }
        }
    }
}

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
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Netty客户端调用器，用于执行远程方法调用
 *
 * @author huangchengxing
 */
@Depends({
    @Depends.Attr(name = Attributes.REMOTE_ADDRESS, type = RemoteAddress.class, required = true),
    @Depends.Attr(name = Attributes.REQUEST_TIMEOUT, type = Long.class, required = true),
    @Depends.Attr(name = Attributes.REQUEST_TIMEOUT_UNIT, type = TimeUnit.class, required = true),
    @Depends.Attr(name = Attributes.REQUEST_PROTOCOL_VERSION, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.COMPRESSION_TYPE, type = Byte.class, required = true),
    @Depends.Attr(name = Attributes.SERIALIZATION_TYPE, type = Byte.class, required = true)
})
@Slf4j
public class NettyClientRequestHandler implements RpcInvocationHandler, Closeable {

    private final ConcurrentMap<String, CompletableFuture<Object>> uncompletedRequests = new ConcurrentHashMap<>();
    private final Bootstrap bootstrap;
    private final EventLoopGroup worker;

    /**
     * 连接超时时间
     */
    @Setter
    private long connectTimeout = 1000L;

    /**
     * 默认连接超时时间单位
     */
    @Setter
    private TimeUnit connectTimeoutUnit = TimeUnit.MILLISECONDS;

    public NettyClientRequestHandler(
        ComponentManager componentManager) {
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
    protected Channel getChannel(InetSocketAddress address) {
        CompletableFuture<Channel> future = new CompletableFuture<>();
        bootstrap.connect(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                future.complete(channel);
                log.info("客户端与服务端连接成功，服务端地址[{}]，通道ID为[{}]", address.toString(), channel.id());
            } else {
                future.completeExceptionally(channelFuture.cause());
            }
        });
        try {
            return future.get(connectTimeout, connectTimeoutUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Rpc4jException("客户端连接被中断", e);
        } catch (ExecutionException e) {
            throw new Rpc4jException("客户端连接发生异常", e);
        } catch (TimeoutException e) {
            throw new Rpc4jException("客户端连接超时: {} {}", connectTimeout, connectTimeoutUnit.name().toLowerCase(), e);
        }
    }

    /**
     * 建立链接，发送请求并获得响应
     *
     * @param channel 通道
     * @param rpcInvocation 调用参数
     * @return 响应
     */
    private Response doConnect(Channel channel, RpcInvocation rpcInvocation) {
        Asserts.isTrue(channel.isActive(), "连接[{}]已经关闭！", channel.id());

        Request request = new Request(
            UUID.randomUUID().toString(),
            rpcInvocation.getTargetName(), rpcInvocation.getMethodName(),
            rpcInvocation.getParameterTypes(), rpcInvocation.getArguments()
        );

        Long timeout = rpcInvocation.getAttribute(Attributes.REQUEST_TIMEOUT);
        TimeUnit timeUnit = rpcInvocation.getAttribute(Attributes.REQUEST_TIMEOUT_UNIT);
        byte protocolVersion = rpcInvocation.getAttribute(Attributes.REQUEST_PROTOCOL_VERSION);
        byte compressionType = rpcInvocation.getAttribute(Attributes.COMPRESSION_TYPE);
        byte serializationType = rpcInvocation.getAttribute(Attributes.SERIALIZATION_TYPE);
        byte messageType = Rpc4jProtocol.MessageType.RPC_REQUEST.getCode();

        // 创建并注册任务
        String requestId = request.getRequestId();
        var uncompletedRequest = uncompletedRequests.computeIfAbsent(
            requestId, id -> new CompletableFuture<>()
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
                uncompletedRequest.completeExceptionally(future.cause());
            }
        });

        // 阻塞当前线程，直到超时或任务完成
        // TODO 可选是否选用全异步模式
        try {
            return (Response)uncompletedRequest.get(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Rpc4jException("客户端请求被中断，请求ID为[{}]", requestId, e);
        } catch (ExecutionException e) {
            throw new Rpc4jException("客户端请求发生异常，请求ID为[{}]", requestId, e);
        } catch (TimeoutException e) {
            throw new Rpc4jException("客户端请求超时，请求ID为[{}]", requestId, e);
        } finally {
            uncompletedRequests.remove(requestId);
        }
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
                var request = uncompletedRequests.get(response.getRequestId());
                if (Objects.nonNull(request) && !request.isDone()) {
                    request.complete(response);
                }
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
}

package io.github.createsequence.rpc4j.core.transport.netty.client;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.client.Connection;
import io.github.createsequence.rpc4j.core.client.ConnectionFactory;
import io.github.createsequence.rpc4j.core.compress.CompressionType;
import io.github.createsequence.rpc4j.core.serialize.SerializationType;
import io.github.createsequence.rpc4j.core.support.DefaultRequest;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import io.github.createsequence.rpc4j.core.transport.PacketType;
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.github.createsequence.rpc4j.core.transport.ResponseStatus;
import io.github.createsequence.rpc4j.core.transport.Rpc4jProtocolBean;
import io.github.createsequence.rpc4j.core.transport.netty.codec.Rpc4jNettyDecoder;
import io.github.createsequence.rpc4j.core.transport.netty.codec.Rpc4jNettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Netty连接工厂
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class NettyClientConnectionFactory implements ConnectionFactory {

    // TODO 使用连接池复用Channel

    private final ProtocolVersion protocolVersion;

    private final RequestRegistry requestRegistry;
    private final Bootstrap bootstrap;
    private final EventLoopGroup worker;

    public NettyClientConnectionFactory(
        ProtocolVersion protocolVersion, ComponentManager componentManager) {

        this.protocolVersion = protocolVersion;

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
                    pipeline.addLast(new Rpc4jNettyDecoder(componentManager, protocolVersion));
                    pipeline.addLast(new Rpc4jNettyClientHandler(requestRegistry));
                }
            });
    }

    /**
     * 获取连接
     *
     * @param address 地址
     * @return 连接
     */
    @Override
    public Connection getConnection(InetSocketAddress address) {
        Channel channel = getChannel(address);
        return new NettyChannelConnection(channel);
    }

    /**
     * 获取通道
     *
     * @param address 地址
     * @return 通道
     */
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

    @Override
    public void close() {
        worker.shutdownGracefully();
    }

    @RequiredArgsConstructor
    protected class NettyChannelConnection implements Connection {

        private final Channel channel;

        /**
         * 获取连接ID
         *
         * @return 连接ID
         */
        @Override
        public String getId() {
            return channel.id().asLongText();
        }

        /**
         * 连接是否活跃
         *
         * @return 是否活跃
         */
        @Override
        public boolean isActive() {
            return channel.isActive();
        }

        /**
         * 建立链接，发送请求并获得响应
         *
         * @param request 请求
         * @param timeout 超时时间
         * @param timeUnit 时间单位
         * @return 响应
         */
        @SneakyThrows
        @Override
        public Response connect(Request request, long timeout, TimeUnit timeUnit) {
            Asserts.isTrue(isActive(), "连接[{}]已经关闭！", getId());

            // 创建并注册任务
            String requestId = request.getRequestId();
            var requestFuture = requestRegistry.register(
                request.getRequestId(), timeout, timeUnit, null
            );

            // 通过通道异步发送请求
            channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端发送请求成功，请求ID为[{}]", requestId);
                } else {
                    log.error("客户端发送请求失败，请求ID为[{}]", requestId);
                    requestRegistry.complete(requestId, future.cause());
                }
            });

            // 阻塞当前线程，直到超时或任务完成
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
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Rpc4jException(e);
            }
        }
    }
}

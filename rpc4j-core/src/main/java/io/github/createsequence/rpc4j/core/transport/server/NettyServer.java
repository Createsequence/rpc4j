package io.github.createsequence.rpc4j.core.transport.server;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.exception.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.support.handler.DefaultInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocation;
import io.github.createsequence.rpc4j.core.support.handler.RpcInvocationHandler;
import io.github.createsequence.rpc4j.core.transport.Attributes;
import io.github.createsequence.rpc4j.core.transport.Message;
import io.github.createsequence.rpc4j.core.transport.Request;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyDecoder;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * 基于Netty的NIO服务器
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class NettyServer extends AbstractServer {

    private final ComponentManager componentManager;
    private final RpcInvocationHandler invocationHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ServerBootstrap bootstrap;
    private Channel channel;

    /**
     * 启动服务
     */
    @Override
    protected void doStart(String host, int port) {
        this.bootstrap = prepareServerBootstrap();
        try {
            // 绑定端口，绑定后即服务启动，此时可以接受客户端的连接
            ChannelFuture future = bootstrap.bind(host, getPort()).sync();
            // 等待服务端监听端口关闭
            this.channel = future.channel();
            log.info("netty服务器开始监听端口[{}]......", getPort());
            this.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Rpc4jException(e);
        }
    }

    protected ServerBootstrap prepareServerBootstrap() {
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
            .childOption(ChannelOption.TCP_NODELAY, true)
            // 是否开启 TCP 底层心跳机制
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
            .option(ChannelOption.SO_BACKLOG, 128)
            //.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new Rpc4jNettyEncoder(componentManager));
                    pipeline.addLast(new Rpc4jNettyDecoder(componentManager));
                    pipeline.addLast(new ServerInboundHandler());
                }
            });
        return bootstrap;
    }

    /**
     * 准备RPC调用上下文
     *
     * @param message 消息
     * @param request 请求
     * @return RPC调用上下文
     */
    protected RpcInvocation prepareRpcInvocation(Message<?> message, Request request) {
        RpcInvocation rpcInvocation = new DefaultInvocation(
            Collections.emptyList(),
            request.getTargetName(), request.getMethodName(),
            request.getParameterTypes(), request.getArguments()
        );
        rpcInvocation.setAttribute(Attributes.REQUEST_ID, request.getRequestId());

        // 响应头消息
        rpcInvocation.setAttribute(Attributes.SERIALIZATION_TYPE, message.getSerializationType());
        rpcInvocation.setAttribute(Attributes.COMPRESSION_TYPE, message.getCompressType());
        rpcInvocation.setAttribute(Attributes.REQUEST_PROTOCOL_VERSION, message.getVersion());
        return rpcInvocation;
    }

    /**
     * 停止服务
     */
    @Override
    public void doStop() {
        channel.close();
        channel.closeFuture();
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    /**
     * 服务端入站请求处理器，目前仅可能收到{@link Request}请求
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public class ServerInboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object target) {
            if (target instanceof Message<?> message
                && message.getPayload() instanceof Request request) {
                log.info("服务端收到消息：{}", message);
                Asserts.isNotNull(request, "请求参数为空！");
                log.info("服务端处理请求，请求ID为[{}]", request.getRequestId());
                RpcInvocation rpcInvocation = prepareRpcInvocation(message, request);
                handle(ctx, rpcInvocation);
            }
        }

        private void handle(ChannelHandlerContext ctx, RpcInvocation rpcInvocation) {
            // 完成调用
            try {
                Object response = invocationHandler.invoke(rpcInvocation);
                if (ctx.channel().isActive() || ctx.channel().isWritable()) {
                    ctx.writeAndFlush(response);
                    log.info("服务端返回响应数据，请求ID为[{}]", rpcInvocation.<String>getAttribute(Attributes.REQUEST_ID));
                } else {
                    throw new Rpc4jException("服务端通道不可用");
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                log.error("服务端请求处理异常！", ex);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error("服务端请求拦截异常！", cause);
            cause.printStackTrace();
            ctx.close();
        }
    }
}

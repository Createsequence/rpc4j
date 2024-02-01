package io.github.createsequence.rpc4j.core.transport.netty.server;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.rpc4j.core.server.AbstractServer;
import io.github.createsequence.rpc4j.core.server.RequestHandler;
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.github.createsequence.rpc4j.core.transport.netty.client.Rpc4jNettyClientHandler;
import io.github.createsequence.rpc4j.core.transport.netty.codec.Rpc4jNettyDecoder;
import io.github.createsequence.rpc4j.core.transport.netty.codec.Rpc4jNettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于Netty的NIO服务器
 *
 * @author huangchengxing
 */
@Slf4j
public class NettyServer extends AbstractServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;
    private final String host;
    private final ServerBootstrap bootstrap;
    private Channel channel;

    public NettyServer(
        String host, int port, ProtocolVersion protocolVersion,
        RequestHandler requestHandler, ComponentManager componentManager) {
        this(
            host, port, protocolVersion,
            new NioEventLoopGroup(), new NioEventLoopGroup(),
            requestHandler, componentManager
        );
    }

    /**
     * 创建一个RPC服务器
     *
     * @param host 主机
     * @param port 端口号
     * @param bossGroup 主线程组
     * @param workGroup 工作线程组
     */
    protected NettyServer(
        String host, int port, ProtocolVersion protocolVersion,
        EventLoopGroup bossGroup, EventLoopGroup workGroup,
        RequestHandler requestHandler, ComponentManager componentManager) {
        super(port);
        this.bossGroup = bossGroup;
        this.workGroup = workGroup;
        this.host = host;

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
                    pipeline.addLast(new Rpc4jNettyDecoder(componentManager, protocolVersion));
                    pipeline.addLast(new Rpc4jNettyServerHandler(requestHandler));
                }
            });
    }

    /**
     * 启动服务
     */
    @Override
    protected void doStart() {
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
}

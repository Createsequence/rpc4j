package io.github.createsequence.rpc4j.core.transport.netty.server;

import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import io.github.createsequence.rpc4j.core.server.RequestHandler;
import io.github.createsequence.rpc4j.core.support.DefaultResponse;
import io.github.createsequence.rpc4j.core.support.Request;
import io.github.createsequence.rpc4j.core.support.Response;
import io.github.createsequence.rpc4j.core.transport.PacketType;
import io.github.createsequence.rpc4j.core.transport.ResponseStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 服务端入站请求处理器，目前仅可能收到{@link Request}请求
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class Rpc4jNettyServerHandler extends ChannelInboundHandlerAdapter {

    private final RequestHandler requestHandler;

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Request request) {
            handleRequest(ctx, request);
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, Request request) {
        Asserts.isNotNull(request, "请求参数为空！");
        log.info("服务接受请求：{}", request.getRequestId());

        // 完成调用
        Response response = null;
        Throwable throwable = null;
        try {
            response = requestHandler.handle(request);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throwable = ex;
        }

        // 如果执行过程中抛出异常，则请求失败
        if (Objects.nonNull(throwable)) {
            response = new DefaultResponse(
                request.getRequestId(), null, throwable
            );
        }
        // 若服务的通道不可写，则请求失败
        else if (!ctx.channel().isActive() || !ctx.channel().isWritable()) {
            response.setThrowable(new Rpc4jException("服务端通道不可用"));
        }
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("服务端请求拦截异常！", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

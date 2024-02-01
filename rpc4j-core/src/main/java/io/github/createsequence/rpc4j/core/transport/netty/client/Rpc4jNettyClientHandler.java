package io.github.createsequence.rpc4j.core.transport.netty.client;

import io.github.createsequence.rpc4j.core.support.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端入站请求处理器，目前仅可能收到服务端的{@link Response}响应
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class Rpc4jNettyClientHandler extends ChannelInboundHandlerAdapter {

    private final RequestRegistry requestRegistry;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("客户端接收到消息：{}", msg);
        if (msg instanceof Response response) {
            log.info("客户端已收到请求[{}]的响应", response.getRequestId());
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

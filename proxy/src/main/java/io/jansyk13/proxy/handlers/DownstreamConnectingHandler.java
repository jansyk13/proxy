package io.jansyk13.proxy.handlers;

import io.jansyk13.proxy.downstream.DownstreamChannelDisposer;
import io.jansyk13.proxy.events.DownstreamChannelEvent;
import io.jansyk13.proxy.events.DownstreamChannelWritabilityChangedEvent;
import io.jansyk13.proxy.events.DownstreamReadCompletedEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class DownstreamConnectingHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final DownstreamChannelDisposer downstreamDownstreamChannelDisposer;
    private final Channel upstreamChannel;

    //TODO replace with AtomicIntegerFieldUpdater
    private AtomicBoolean disposed = new AtomicBoolean(false);

    public DownstreamConnectingHandler(DownstreamChannelDisposer downstreamDownstreamChannelDisposer, Channel upstreamChannel) {
        this.downstreamDownstreamChannelDisposer = downstreamDownstreamChannelDisposer;
        this.upstreamChannel = upstreamChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        // increase by two because channelRead in SimpleChannelInboundHandler releases and write
        // to upstream channel is future
        upstreamChannel.writeAndFlush(ReferenceCountUtil.retain(msg, 2));
        if (msg instanceof LastHttpContent) {
            dispose(ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (upstreamChannel.isActive()) {
            upstreamChannel.pipeline().fireUserEventTriggered(new DownstreamReadCompletedEvent());
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (upstreamChannel.isActive()) {
            upstreamChannel.pipeline().fireUserEventTriggered(
                    new DownstreamChannelWritabilityChangedEvent(ctx.channel().isWritable())
            );
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (upstreamChannel.isActive() && evt instanceof DownstreamChannelEvent) {
            upstreamChannel.pipeline().fireUserEventTriggered(evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        dispose(ctx);
    }


    private void dispose(ChannelHandlerContext ctx) {
        if (disposed.compareAndSet(false, true)) {
            ctx.pipeline().remove(this);
            downstreamDownstreamChannelDisposer.dispose(ctx.channel());
        }
    }
}

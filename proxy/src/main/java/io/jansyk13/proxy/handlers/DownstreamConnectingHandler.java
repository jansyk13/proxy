package io.jansyk13.proxy.handlers;

import io.jansyk13.proxy.client.ChannelDisposer;
import io.jansyk13.proxy.events.DownstreamChannelEvent;
import io.jansyk13.proxy.events.DownstreamChannelWritabilityChangedEvent;
import io.jansyk13.proxy.events.DownstreamReadCompletedEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class DownstreamConnectingHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final ChannelDisposer downstreamChannelDisposer;
    private final Channel upstreamChannel;

    //TODO replace with AtomicIntegerFieldUpdater
    private AtomicBoolean disposed = new AtomicBoolean(false);

    public DownstreamConnectingHandler(ChannelDisposer downstreamChannelDisposer, Channel upstreamChannel) {
        this.downstreamChannelDisposer = downstreamChannelDisposer;
        this.upstreamChannel = upstreamChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        upstreamChannel.writeAndFlush(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (upstreamChannel.isActive()) {
            upstreamChannel.pipeline().fireUserEventTriggered(new DownstreamReadCompletedEvent());
        }
        dispose(ctx);
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
            downstreamChannelDisposer.dispose(ctx.channel());
        }
    }
}

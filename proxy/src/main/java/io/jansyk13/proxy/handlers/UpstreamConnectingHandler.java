package io.jansyk13.proxy.handlers;

import io.jansyk13.proxy.downstream.DownstreamChannelManager;
import io.jansyk13.proxy.events.UpstreamChannelEvent;
import io.jansyk13.proxy.events.UpstreamChannelWritabilityChangedEvent;
import io.jansyk13.proxy.events.UpstreamReadCompletedEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;

/**
 * Inbound handler responsible for propagating messages, exception and events to downstream channel. Also is
 * responsible for initial read and setting auto read to false. After acquiring downstream attempts to start
 * reading.
 */
public class UpstreamConnectingHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final DownstreamChannelManager downstreamDownstreamChannelManager;
    private Promise<Channel> downstreamChannel;

    /**
     * Inbound handler connecting upstream channel to downstream channel
     *
     * @param downstreamDownstreamChannelManager provider which is takes {@link HttpRequest} and {@link ChannelHandlerContext}
     *                                           as inputs
     */
    public UpstreamConnectingHandler(DownstreamChannelManager downstreamDownstreamChannelManager) {
        this.downstreamDownstreamChannelManager = downstreamDownstreamChannelManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        HttpObject retained = ReferenceCountUtil.retain(msg);
        if (msg instanceof HttpRequest) {
            //stop reading until downstream
            ctx.channel().config().setAutoRead(false);

            downstreamChannel = downstreamDownstreamChannelManager.provide((HttpRequest) msg, ctx).addListener(f -> {
                if (f.isSuccess()) {
                    Channel channel = (Channel) f.getNow();
                    channel.pipeline().addLast(
                            new DownstreamConnectingHandler(downstreamDownstreamChannelManager.disposer(), ctx.channel())
                    );

                    channel.writeAndFlush(retained);

                    // start reading if possible
                    if (channel.isWritable()) {
                        ctx.channel().config().setAutoRead(true);
                    }
                } else {
                    ctx.fireExceptionCaught(f.cause());
                }
            });
        } else {
            // promise might not be fulfilled
            if (!downstreamChannel.isDone()) {
                downstreamChannel.addListener(f -> {
                    if (f.isSuccess()) {
                        Channel channel = (Channel) f.getNow();
                        channel.writeAndFlush(retained);
                    } else {
                        ctx.fireExceptionCaught(f.cause());
                    }
                });
            } else if (downstreamChannel.isSuccess()) {
                downstreamChannel.getNow().writeAndFlush(retained);
            } else {
                // beware of double error transmission
                ctx.fireExceptionCaught(downstreamChannel.cause());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (downstreamChannelPresentAndActive()) {
            downstreamChannel.get().pipeline().fireUserEventTriggered(new UpstreamReadCompletedEvent());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (downstreamChannelPresentAndActive() && evt instanceof UpstreamChannelEvent) {
            downstreamChannel.get().pipeline().fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (downstreamChannelPresentAndActive()) {
            UpstreamChannelWritabilityChangedEvent event = new UpstreamChannelWritabilityChangedEvent(ctx.channel().isWritable());
            downstreamChannel.get().pipeline().fireUserEventTriggered(event);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (downstreamChannelPresentAndActive()) {
            downstreamChannel.get().pipeline().fireExceptionCaught(cause);
        }
        ctx.channel().pipeline().remove(this);
        ctx.close();
    }

    private boolean downstreamChannelPresentAndActive() {
        return downstreamChannel.isSuccess() && downstreamChannel.getNow().isActive() ? true : false;
    }
}

package io.jansyk13.proxy.downstream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

public class SimpleDownstreamChannelManager extends DownstreamChannelManager {

    private final Bootstrap bootstrap;

    public SimpleDownstreamChannelManager(final DownstreamBootstrapSpec downstreamBootstrapSpec) {
        super(downstreamBootstrapSpec);
        this.bootstrap = buildBootstrap();
    }

    @Override
    public Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
        Bootstrap clone = bootstrap.clone();

        DefaultPromise<Channel> promise = new DefaultPromise<>(downstreamBootstrapSpec.getEventLoopGroup().next());
        ChannelFuture channelFuture = clone.connect(downstreamBootstrapSpec.getAddress(), downstreamBootstrapSpec.getPort());

        channelFuture.addListener(f -> {
            if (f.isSuccess()) {
                promise.setSuccess(channelFuture.channel());
            } else {
                promise.setFailure(f.cause());
            }
        });

        return promise;
    }

    @Override
    public DownstreamChannelDisposer disposer() {
        return channel -> channel.closeFuture();
    }
}
